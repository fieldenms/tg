package ua.com.fielden.platform.web_api;

import static graphql.GraphqlErrorBuilder.newError;
import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web_api.FieldSchema.DEFAULT_PAGE_CAPACITY;
import static ua.com.fielden.platform.web_api.FieldSchema.DEFAULT_PAGE_NUMBER;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_CAPACITY;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_NUMBER;
import static ua.com.fielden.platform.web_api.RootEntityUtils.extractValue;
import static ua.com.fielden.platform.web_api.RootEntityUtils.generateQueryModelFrom;
import static ua.com.fielden.platform.web_api.RootEntityUtils.rootPropAndArguments;

import java.util.List;
import java.util.Optional;

import graphql.execution.DataFetcherResult;
import graphql.execution.DataFetcherResult.Builder;
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
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.IDates;

/**
 * {@link DataFetcher} implementation responsible for resolving root {@code Query} fields that correspond to main entity trees.
 * All other {@link DataFetcher}s for sub-fields can be left unchanged ({@link PropertyDataFetcher}) unless some specific behaviour is needed.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class RootEntityFetcher<T extends AbstractEntity<?>> implements DataFetcher<DataFetcherResult<List<T>>> {
    private final Class<T> entityType;
    private final ICompanionObjectFinder coFinder;
    private final IDates dates;
    private final IAuthorisationModel authorisation;
    private final String securityTokensPackageName;
    
    /**
     * Creates {@link RootEntityFetcher} for concrete {@code entityType}.
     * 
     * @param entityType
     * @param coFinder
     * @param dates
     * @param authorisation
     * @param securityTokensPackageName
     */
    public RootEntityFetcher(final Class<T> entityType, final ICompanionObjectFinder coFinder, final IDates dates, final IAuthorisationModel authorisation, final String securityTokensPackageName) {
        this.entityType = entityType;
        this.coFinder = coFinder;
        this.dates = dates;
        this.authorisation = authorisation;
        this.securityTokensPackageName = securityTokensPackageName;
    }
    
    /**
     * Checks whether {@code entityType} can be executed by current user and returns error if not.
     * Otherwise, finds an uninstrumented reader for the {@link #entityType} and retrieves first {@link #PAGE_CAPACITY} entities.<p>
     * {@inheritDoc}
     */
    @Override
    public DataFetcherResult<List<T>> get(final DataFetchingEnvironment environment) {
        ofNullable(
            findToken(securityTokensPackageName, entityType.getSimpleName(), READ)
            .orElseGet(() -> findDefaultToken(securityTokensPackageName, READ))
        )   .map(token -> authorisation.authorise(token))
            .orElseGet(() -> failure(format("Read token has not been found for %s.", entityType.getSimpleName())))
            .ifFailure(Result::throwRuntime);
        final T3<String, List<GraphQLArgument>, List<Argument>> rootArguments = rootPropAndArguments(environment.getGraphQLSchema(), environment.getField());
        final T2<Optional<String>, QueryExecutionModel<T, EntityResultQueryModel<T>>> warningAndModel = generateQueryModelFrom(
            environment.getField(),
            environment.getVariables(),
            environment.getFragmentsByName(),
            entityType,
            environment.getGraphQLSchema()
        ).apply(dates);
        final Builder<List<T>> result = DataFetcherResult.<List<T>>newResult().data(coFinder.findAsReader(entityType, true).getPage( // reader must be uninstrumented
            warningAndModel._2,
            extractValue(
                PAGE_NUMBER,
                t2(rootArguments._2, rootArguments._3),
                environment.getVariables(),
                environment.getGraphQLSchema().getCodeRegistry(),
                0
            ).orElse(DEFAULT_PAGE_NUMBER),
            extractValue(
                PAGE_CAPACITY,
                t2(rootArguments._2, rootArguments._3),
                environment.getVariables(),
                environment.getGraphQLSchema().getCodeRegistry(),
                1
            ).orElse(DEFAULT_PAGE_CAPACITY)
        ).data());
        warningAndModel._1.ifPresent(warning -> result.error(newError(environment).message(warning).build()));
        return result.build();
    }
    
    public static Optional<Class<? extends ISecurityToken>> findToken(final String securityTokensPackageName, final String entityTypeSimpleName, final Template tokenKind) {
        try {
            return of(findTokenByName(securityTokensPackageName, tokenKind, ".persistent.", entityTypeSimpleName));
        } catch (final ClassNotFoundException notFound1) {
            try {
                return of(findTokenByName(securityTokensPackageName, tokenKind, ".synthetic.", entityTypeSimpleName));
            } catch (final ClassNotFoundException notFound2) {
                return empty();
            }
        }
    }
    
    private static Class<? extends ISecurityToken> findTokenByName(final String securityTokensPackageName, final Template tokenKind, final String packagePart, final String templateParam) throws ClassNotFoundException {
        return (Class<? extends ISecurityToken>) forName(securityTokensPackageName + packagePart + format(tokenKind.forClassName(), templateParam));
    }
    
    public static Class<? extends ISecurityToken> findDefaultToken(final String securityTokensPackageName, final Template tokenKind) {
        try {
            return findTokenByName(securityTokensPackageName, tokenKind, ".persistent.", "");
        } catch (final ClassNotFoundException notFound) {
            return null;
        }
    }
    
}