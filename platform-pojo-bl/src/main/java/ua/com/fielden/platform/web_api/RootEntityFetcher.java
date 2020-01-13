package ua.com.fielden.platform.web_api;

import static java.lang.String.format;
import static ua.com.fielden.platform.web_api.RootEntityMixin.generateQueryModelFrom;

import java.util.List;

import org.apache.log4j.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class RootEntityFetcher implements DataFetcher {
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<? extends AbstractEntity<?>> entityType;
    private final ICompanionObjectFinder coFinder;
    
    public RootEntityFetcher(final Class<? extends AbstractEntity<?>> entityType, final ICompanionObjectFinder coFinder) {
        this.entityType = entityType;
        this.coFinder = coFinder;
    }
    
    @Override
    public Object get(final DataFetchingEnvironment environment) {
        try {
            logger.error(format("Quering type [%s]...", entityType.getSimpleName()));
            logger.error("\tSource " + environment.getSource());
            logger.error(format("\tArguments [%s]", environment.getArguments()));
            logger.error("\tContext " + environment.getContext());
            logger.error("\tLocalContext " + environment.getLocalContext());
            logger.error("\troot " + environment.getRoot());
            
            logger.error(format("\tField [%s]", environment.getField()));
            logger.error(format("\tMergedField [%s]", environment.getMergedField()));
            logger.error(format("\tParentType [%s]", environment.getParentType()));
            
            logger.error(format("\tVariables [%s]", environment.getVariables()));
            logger.error(format("\tFragmentsByName [%s]", environment.getFragmentsByName()));
            
            final QueryExecutionModel queryModel = generateQueryModelFrom(environment.getField().getSelectionSet(), environment.getVariables(), environment.getFragmentsByName(), entityType);
            
            final IEntityDao<? extends AbstractEntity> co = coFinder.find(entityType);
            final List entities = co.getAllEntities(queryModel); // TODO fetch order etc.
            logger.error(String.format("Quering type [%s]...done", entityType.getSimpleName()));
            return entities;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(String.format("Quering type [%s]...done", entityType.getSimpleName()));
            // TODO improve errors handling
            throw new IllegalStateException(e);
        }
    }
}