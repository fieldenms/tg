package ua.com.fielden.platform.web_api;

import static ua.com.fielden.platform.web_api.RootEntityMixin.generateQueryModelFrom;

import java.util.List;

import org.apache.log4j.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class RootEntityFetcher<T extends AbstractEntity<?>> implements DataFetcher<List<T>> {
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<T> entityType;
    private final ICompanionObjectFinder coFinder;
    
    public RootEntityFetcher(final Class<T> entityType, final ICompanionObjectFinder coFinder) {
        this.entityType = entityType;
        this.coFinder = coFinder;
    }
    
    @Override
    public List<T> get(final DataFetchingEnvironment environment) {
        try {
//            logger.error(format("Quering type [%s]...", entityType.getSimpleName()));
//            logger.error("\tSource " + environment.getSource());
//            logger.error(format("\tArguments [%s]", environment.getArguments()));
//            logger.error("\tContext " + environment.getContext());
//            logger.error("\tLocalContext " + environment.getLocalContext());
//            logger.error("\troot " + environment.getRoot());
//            
//            logger.error(format("\tField [%s]", environment.getField()));
//            logger.error(format("\tMergedField [%s]", environment.getMergedField()));
//            logger.error(format("\tParentType [%s]", environment.getParentType()));
//            
//            logger.error(format("\tVariables [%s]", environment.getVariables()));
//            logger.error(format("\tFragmentsByName [%s]", environment.getFragmentsByName()));
            
            final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel = generateQueryModelFrom(environment.getField().getSelectionSet(), environment.getVariables(), environment.getFragmentsByName(), entityType, environment.getGraphQLSchema());
            
            final List<T> entities = coFinder.findAsReader(entityType, true).getFirstEntities(queryModel, 1000);
//            logger.error(String.format("Quering type [%s]...done", entityType.getSimpleName()));
            return entities;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
//            logger.error(String.format("Quering type [%s]...done", entityType.getSimpleName()));
            // TODO improve errors handling
            throw new IllegalStateException(e);
        }
    }
}