package ua.com.fielden.platform.web.utils;

import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;

public class EntityRestorationUtils {
    private static final Logger logger = Logger.getLogger(EntityRestorationUtils.class);
    
    ////////////////////////////////////// VALIDATION PROTOTYPE CREATION //////////////////////////////////////
    /**
     * Initialises the entity for retrieval.
     *
     * @param id
     *            -- entity identifier
     * @return
     */
    public static <T extends AbstractEntity<?>> T createValidationPrototype(
        final Long id, 
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer
    ) {
        final T entity;
        if (id != null) {
            entity = companion.findById(id, companion.getFetchProvider().fetchModel());
        } else {
            entity = producer.newEntity();
        }
        return entity;
    }

    /**
     * Initialises the functional entity for centre-context-dependent retrieval.
     *
     * @param centreContext
     *            the context for functional entity creation
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> T createValidationPrototypeWithContext(
        final Long id,
        final CentreContext<T, AbstractEntity<?>> centreContext,
        final String chosenProperty,
        final Long compoundMasterEntityId,
        final AbstractEntity<?> masterContext,
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer
    ) {
        if (id != null) {
            return companion.findById(id, companion.getFetchProvider().fetchModel());
        } else {
            final DefaultEntityProducerWithContext<T> defProducer = (DefaultEntityProducerWithContext<T>) producer;
            defProducer.setCentreContext(centreContext);
            defProducer.setChosenProperty(chosenProperty);
            defProducer.setCompoundMasterEntityId(compoundMasterEntityId);
            defProducer.setMasterEntity(masterContext);
            return defProducer.newEntity();
        }
    }
    
    ////////////////////////////////////// ENTITY CONSTRUCTION //////////////////////////////////////
    /**
     * Constructs the entity from the client envelope.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntity(
        final Map<String, Object> modifiedPropertiesHolder,
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer,
        final ICompanionObjectFinder companionFinder
    ) {
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");

        return applyModifHolder(modifiedPropertiesHolder, createValidationPrototype(id, companion, producer), companionFinder);
    }
    
    /**
     * Constructs the entity from the client envelope.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @param envelope
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntityWithContext(
        final Map<String, Object> modifiedPropertiesHolder,
        final CentreContext<T, AbstractEntity<?>> centreContext,
        final String chosenProperty,
        final Long compoundMasterEntityId,
        final AbstractEntity<?> masterContext, 
        final int tabCount,
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer,
        final ICompanionObjectFinder companionFinder
    ) {
        logger.debug(EntityResourceUtils.tabs(tabCount) + "constructEntity: started.");
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");

        final T validationPrototypeWithContext = createValidationPrototypeWithContext(id, centreContext, chosenProperty, compoundMasterEntityId, masterContext, companion, producer);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "constructEntity: validationPrototypeWithContext.");
        final Pair<T, Map<String, Object>> constructed = applyModifHolder(modifiedPropertiesHolder, validationPrototypeWithContext, companionFinder);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "constructEntity: finished.");
        return constructed;
    }
    
    private static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> applyModifHolder(
        final Map<String, Object> modifiedPropertiesHolder, 
        final M validationPrototype, 
        final ICompanionObjectFinder companionFinder
    ) {
        return new Pair<>(EntityResourceUtils.apply(modifiedPropertiesHolder, validationPrototype, companionFinder), modifiedPropertiesHolder);
    }
}
