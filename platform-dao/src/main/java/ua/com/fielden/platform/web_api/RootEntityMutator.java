package ua.com.fielden.platform.web_api;

import static java.lang.String.format;
import static ua.com.fielden.platform.web_api.RootEntityMixin.generateQueryModelFrom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityResourceContinuationsHelper;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.Pair;

public class RootEntityMutator<T extends AbstractEntity<?>> implements DataFetcher<T> {
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<T> entityType;
    private final ICompanionObjectFinder coFinder;
    private final EntityFactory entityFactory;
    
    public RootEntityMutator(final Class<T> entityType, final ICompanionObjectFinder coFinder, final EntityFactory entityFactory) {
        this.entityType = entityType;
        this.coFinder = coFinder;
        this.entityFactory = entityFactory;
    }
    
    @Override
    public T get(final DataFetchingEnvironment environment) {
        try {
            final IEntityDao<T> co = coFinder.find(entityType);
            final SavingInfoHolder savingInfoHolder = createSavingInfoHolder(environment.getArguments()); // TODO impl
            final Pair<T, Optional<Exception>> potentiallySavedWithException = EntityResourceContinuationsHelper.tryToSave(savingInfoHolder , entityType, entityFactory, coFinder, co);
            
            if (potentiallySavedWithException.getValue().isPresent()) {
                throw potentiallySavedWithException.getValue().get();
            }
            
            logger.error(format("Mutating type [%s]...", entityType.getSimpleName()));
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
            
            final QueryProperty idQueryProperty = new QueryProperty(entityType, AbstractEntity.ID);
            idQueryProperty.setValue(potentiallySavedWithException.getKey().getId());
            idQueryProperty.setValue2(potentiallySavedWithException.getKey().getId());
            final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel = generateQueryModelFrom(environment.getField().getSelectionSet(), environment.getVariables(), environment.getFragmentsByName(), entityType, idQueryProperty);
            
            final T entity = co.getEntity(queryModel);
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