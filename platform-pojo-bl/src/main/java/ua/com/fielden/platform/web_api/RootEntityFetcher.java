package ua.com.fielden.platform.web_api;

import static ua.com.fielden.platform.web_api.RootEntityUtils.generateQueryModelFrom;

import java.util.List;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.PropertyDataFetcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
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
     * Default maximum number of entities returned in a single root field of a <code>Query</code>.
     */
    private final int MAX_NUMBER_OF_ENTITIES = 1000;
    
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
     * Finds an uninstrumented reader for the {@link #entityType} and retrieves first {@link #MAX_NUMBER_OF_ENTITIES} entities.<p>
     * {@inheritDoc}
     */
    @Override
    public List<T> get(final DataFetchingEnvironment environment) {
        return coFinder.findAsReader(entityType, true).getFirstEntities( // reader must be uninstrumented
            generateQueryModelFrom(
                environment.getField().getSelectionSet(),
                environment.getVariables(),
                environment.getFragmentsByName(),
                entityType,
                environment.getGraphQLSchema()
            ).apply(dates),
            MAX_NUMBER_OF_ENTITIES
        );
    }
    
}