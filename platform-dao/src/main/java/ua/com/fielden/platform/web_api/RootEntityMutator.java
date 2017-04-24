package ua.com.fielden.platform.web_api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
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
            final IEntityDao co = coFinder.find(entityType);
            final SavingInfoHolder savingInfoHolder = createSavingInfoHolder(environment.getArguments()); // TODO impl
            final Pair<AbstractEntity, Optional<Exception>> potentiallySavedWithException = (Pair<AbstractEntity, Optional<Exception>>) EntityResourceContinuationsHelper.tryToSave(savingInfoHolder , entityType, entityFactory, coFinder, co);
            
            if (potentiallySavedWithException.getValue().isPresent()) {
                throw potentiallySavedWithException.getValue().get();
            }
            
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
            final QueryProperty idQueryProperty = new QueryProperty(entityType, AbstractEntity.ID);
            idQueryProperty.setValue(potentiallySavedWithException.getKey().getId());
            idQueryProperty.setValue2(potentiallySavedWithException.getKey().getId());
            final QueryExecutionModel queryModel = RootEntityMixin.generateQueryModelFrom(fields, variables, entityType, idQueryProperty);
            
            final AbstractEntity entity = co.getEntity(queryModel); // TODO fetch order etc.
            logger.error(String.format("Mutating type [%s]...done", entityType.getSimpleName()));
            return entity;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(String.format("Mutating type [%s]...done", entityType.getSimpleName()));
            // TODO improve errors handling
            throw new IllegalStateException(e);
        }
    }

    private SavingInfoHolder createSavingInfoHolder(final Map<String, Object> arguments) {
        logger.error(String.format("\tCreate savingInfoHolder: arguments [%s]", arguments));
        final SavingInfoHolder savingInfoHolder = EntityFactory.newPlainEntity(SavingInfoHolder.class, null);
        final Map<String, Object> input = (Map<String, Object>) arguments.get("input");
        final Map<String, Object> modifHolder = new LinkedHashMap<>();
        modifHolder.put("version", 0);
        modifHolder.put("id", null);
        modifHolder.put("@@touchedProps", new ArrayList<String>());
        
        input.entrySet().stream().forEach(nameAndVal -> {
            final Map<String, Object> valObject = new LinkedHashMap<>();
            valObject.put("val", nameAndVal.getValue());
            modifHolder.put(nameAndVal.getKey(), valObject);
        });
        
        savingInfoHolder.setModifHolder(modifHolder);
        return savingInfoHolder;
    }
}