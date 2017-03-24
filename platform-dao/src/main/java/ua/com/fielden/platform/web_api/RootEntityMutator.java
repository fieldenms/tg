package ua.com.fielden.platform.web_api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityResourceContinuationsHelper;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.utils.Pair;

public class RootEntityMutator implements DataFetcher {
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<? extends AbstractEntity<?>> entityType;
    private final ICompanionObjectFinder coFinder;
    private final EntityFactory entityFactory;
    
    public RootEntityMutator(final Class<? extends AbstractEntity<?>> entityType, final ICompanionObjectFinder coFinder, final EntityFactory entityFactory) {
        this.entityType = entityType;
        this.coFinder = coFinder;
        this.entityFactory = entityFactory;
    }
    
    @Override
    public Object get(final DataFetchingEnvironment environment) {
        try {
            final IEntityDao<? extends AbstractEntity> co = coFinder.find(entityType);
            final SavingInfoHolder savingInfoHolder = createSavingInfoHolder(environment.getArguments()); // TODO impl
            final Pair<AbstractEntity, Optional<Exception>> potentiallySavedWithException = EntityResourceContinuationsHelper.<AbstractEntity>tryToSave(savingInfoHolder , (Class<AbstractEntity>) entityType, entityFactory, coFinder, (IEntityDao<AbstractEntity>) co);
            
            logger.error(String.format("Mutating type [%s]...", entityType.getSimpleName()));
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
            
            final AbstractEntity entity = co.getEntity(queryModel); // TODO fetch order etc.
            logger.error(String.format("Mutating type [%s]...done", entityType.getSimpleName()));
            return entity;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(String.format("Mutating type [%s]...done", entityType.getSimpleName()));
            // TODO create graphQL errors to send them on client
            return null;
        }
    }

    private SavingInfoHolder createSavingInfoHolder(final Map<String, Object> arguments) {
        logger.error(String.format("\tArguments [%s]", arguments));
        // TODO Auto-generated method stub
        return null;
    }
}