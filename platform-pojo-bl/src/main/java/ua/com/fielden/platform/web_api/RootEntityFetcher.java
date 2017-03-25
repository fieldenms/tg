package ua.com.fielden.platform.web_api;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import graphql.language.Field;
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
            logger.error(String.format("Quering type [%s]...", entityType.getSimpleName()));
            logger.error(String.format("\tArguments [%s]", environment.getArguments()));
            logger.error(String.format("\tContext [%s]", environment.getContext()));
            logger.error(String.format("\tFields [%s]", environment.getFields()));
            logger.error(String.format("\tFieldType [%s]", environment.getFieldType()));
            logger.error(String.format("\tParentType [%s]", environment.getParentType()));
            logger.error(String.format("\tSource [%s]", environment.getSource()));
            
            final List<Field> fields = environment.getFields();
            // Source was defined in GraphQLQueryResource as "variables". So we can use it here to resolve variables
            final Map<String, Object> variables = (Map<String, Object>) environment.getSource();
            
            final QueryExecutionModel queryModel = RootEntityMixin.generateQueryModelFrom(fields, variables, entityType);
            
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