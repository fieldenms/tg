package ua.com.fielden.platform.web_api;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web_api.FieldSchema.DEFAULT_PAGE_CAPACITY;
import static ua.com.fielden.platform.web_api.FieldSchema.DEFAULT_PAGE_NUMBER;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_CAPACITY;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_NUMBER;
import static ua.com.fielden.platform.web_api.RootEntityUtils.extractValue;
import static ua.com.fielden.platform.web_api.RootEntityUtils.generateQueryModelFrom;
import static ua.com.fielden.platform.web_api.RootEntityUtils.rootPropAndArguments;

import java.util.List;

import graphql.language.Argument;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.PropertyDataFetcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.IDates;

/**
 * {@link DataFetcher} implementation responsible for resolving root <code>Query</code> fields that correspond to main entity trees.
 * All other {@link DataFetcher}s for sub-fields can be left unchanged ({@link PropertyDataFetcher}) unless some specific behaviour is needed.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class RootEntityFetcher<T extends AbstractEntity<?>> implements DataFetcher<List<T>> {
    private final Class<T> entityType;
    private final ICompanionObjectFinder coFinder;
    private final IDates dates;
    
    /**
     * Creates {@link RootEntityFetcher} for concrete <code>entityType</code>.
     * 
     * @param entityType
     * @param coFinder
     * @param dates
     */
    public RootEntityFetcher(final Class<T> entityType, final ICompanionObjectFinder coFinder, final IDates dates) {
        this.entityType = entityType;
        this.coFinder = coFinder;
        this.dates = dates;
    }
    
    /**
     * Finds an uninstrumented reader for the {@link #entityType} and retrieves first {@link #DEFAULT_PAGE_CAPACITY} entities.<p>
     * {@inheritDoc}
     */
    @Override
    public List<T> get(final DataFetchingEnvironment environment) {
        final T3<String, List<GraphQLArgument>, List<Argument>> rootArguments = rootPropAndArguments(environment.getGraphQLSchema(), environment.getField());
        return coFinder.findAsReader(entityType, true).getPage( // reader must be uninstrumented
            generateQueryModelFrom(
                environment.getField(),
                environment.getVariables(),
                environment.getFragmentsByName(),
                entityType,
                environment.getGraphQLSchema()
            ).apply(dates),
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
        ).data();
    }
    
}