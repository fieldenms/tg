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
    private static final Logger LOGGER = Logger.getLogger(RootEntityFetcher.class);
    private final Class<? extends AbstractEntity<?>> entityType;
    private final ICompanionObjectFinder coFinder;
    
    public RootEntityFetcher(final Class<? extends AbstractEntity<?>> entityType, final ICompanionObjectFinder coFinder) {
        this.entityType = entityType;
        this.coFinder = coFinder;
    }
    
    @Override
    public Object get(final DataFetchingEnvironment environment) {
        try {
            LOGGER.error(String.format("Arguments [%s]", environment.getArguments()));
            LOGGER.error(String.format("Context [%s]", environment.getContext()));
            LOGGER.error(String.format("Fields [%s]", environment.getFields()));
            LOGGER.error(String.format("FieldType [%s]", environment.getFieldType()));
            LOGGER.error(String.format("ParentType [%s]", environment.getParentType()));
            LOGGER.error(String.format("Source [%s]", environment.getSource()));
            
            final List<Field> fields = environment.getFields();
            // Source was defined in GraphQLQueryResource as "variables". So we can use it here to resolve variables
            final Map<String, Object> variables = (Map<String, Object>) environment.getSource();
            
            final QueryExecutionModel queryModel = RootEntityMixin.generateQueryModelFrom(fields, variables, entityType);
            
            final IEntityDao<? extends AbstractEntity> co = coFinder.find(entityType);
            final List entities = co.getAllEntities(queryModel); // TODO fetch order etc.
            return entities;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            // TODO create graphQL errors to send them on client
        }
        return null;
    }
}