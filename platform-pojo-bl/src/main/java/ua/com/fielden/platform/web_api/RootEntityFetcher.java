package ua.com.fielden.platform.web_api;

import graphql.execution.DataFetcherResult;
import graphql.language.Argument;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.PropertyDataFetcher;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

import java.util.List;
import java.util.Optional;

import static graphql.GraphqlErrorBuilder.newError;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web_api.FieldSchema.*;
import static ua.com.fielden.platform.web_api.RootEntityUtils.*;

/// A [DataFetcher] implementation responsible for resolving root `Query` fields that correspond to main entity trees.
/// All other [DataFetcher]s for sub-fields can be left unchanged ([PropertyDataFetcher]) unless some specific behaviour is needed.
///
public class RootEntityFetcher<T extends AbstractEntity<?>> implements DataFetcher<DataFetcherResult<List<T>>> {

    public static final String ERR_PAGE_CAPACITY_TOO_LARGE = "Requested page capacity of %s was capped to the limit of %s.";

    private final Class<T> entityType;
    private final ICompanionObjectFinder coFinder;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;
    private final int maxPageCapacity;
    
    public RootEntityFetcher(
            final Class<T> entityType,
            final ICompanionObjectFinder coFinder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final int maxPageCapacity)
    {
        this.entityType = entityType;
        this.coFinder = coFinder;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
        this.maxPageCapacity = maxPageCapacity;
    }

    /// Verifies whether  [#entityType] can be retrieved by the current user and returns an error if access is denied.
    /// Otherwise, finds an uninstrumented reader for [#entityType] and retrieves the first [FieldSchema#PAGE_CAPACITY] entities.
    ///
    /// {@inheritDoc}
    ///
    @Override
    public DataFetcherResult<List<T>> get(final DataFetchingEnvironment environment) {
        authoriseReading(entityType.getSimpleName(), READ, authorisationModel, securityTokenProvider).ifFailure(Result::throwRuntime);// reading of entities should be authorised when running GraphQL query
        final T3<String, List<GraphQLArgument>, List<Argument>> rootArguments = rootPropAndArguments(environment.getGraphQLSchema(), environment.getField());
        final T2<Optional<String>, QueryExecutionModel<T, EntityResultQueryModel<T>>> warningAndModel = generateQueryModelFrom(environment, entityType);

        final var _pageCapacity = getPageCapacity(environment, rootArguments);
        final var pageCapacity = _pageCapacity._1;
        final var maybePageCapacityError = _pageCapacity._2;

        final var pageNo = extractValue(
                PAGE_NUMBER,
                t2(rootArguments._2, rootArguments._3),
                environment.getVariables(),
                environment.getGraphQLSchema().getCodeRegistry(),
                environment.getGraphQlContext(),
                environment.getLocale(),
                0)
                .orElse(DEFAULT_PAGE_NUMBER);
        final var co = coFinder.findAsReader(entityType, true);
        final var result = DataFetcherResult.<List<T>>newResult()
                .data(co.getPage(warningAndModel._2, pageNo, pageCapacity).data());
        warningAndModel._1.ifPresent(warning -> result.error(newError(environment).message(warning).build()));
        maybePageCapacityError.ifPresent(it -> result.error(newError(environment).message(it).build()));
        return result.build();
    }

    private T2<Integer, Optional<String>> getPageCapacity(
            final DataFetchingEnvironment environment,
            final T3<String, List<GraphQLArgument>, List<Argument>> rootArguments)
    {
        final var inPageCapacity = extractValue(
                PAGE_CAPACITY,
                t2(rootArguments._2, rootArguments._3),
                environment.getVariables(),
                environment.getGraphQLSchema().getCodeRegistry(),
                environment.getGraphQlContext(),
                environment.getLocale(),
                1).orElse(DEFAULT_PAGE_CAPACITY);
        final var pageCapacity = Math.min(maxPageCapacity, inPageCapacity);
        final Optional<String> maybePageCapacityError = inPageCapacity > maxPageCapacity
                ? Optional.of(ERR_PAGE_CAPACITY_TOO_LARGE.formatted(inPageCapacity, maxPageCapacity))
                : Optional.empty();
        return t2(pageCapacity, maybePageCapacityError);
    }

}
