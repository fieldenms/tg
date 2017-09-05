package ua.com.fielden.platform.web.utils;

import static ua.com.fielden.platform.web.utils.EntityResourceUtils.tabs;

import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityProducingException;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A set of utilities for creating validation prototype through 1) retrieval from database 2) usage of already produced instance or 3) producing it with / without context.
 * Also contains methods to fully construct entity: application of <code>modifiedPropertiesHolder</code> included.
 * 
 * @author TG Team
 *
 */
public class EntityRestorationUtils {
    private static final Logger logger = Logger.getLogger(EntityRestorationUtils.class);
    
    ////////////////////////////////////// VALIDATION PROTOTYPE CREATION //////////////////////////////////////
    /**
     * Constructs validation prototype having an <code>id</code> and <code>originallyProducedEntity</code> information.
     *
     * @param id -- validation prototype's identifier for retrieval from database or <code>null</code> if 'new' validation prototype is about to create
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this parameter contains originally produced entity instance not to produce it multiple times;
     *                                    it can also be intentionally <code>null</code> to trigger creation through producer
     * 
     * @return
     */
    public static <T extends AbstractEntity<?>> T createValidationPrototype(
        final Long id,
        final T originallyProducedEntity,
        
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer
    ) {
        if (producer == null) {
            throw new EntityProducingException("Producer does not exist during validation prototype creation.");
        }
        final T entity;
        if (id != null) {
            entity = companion.findById(id, companion.getFetchProvider().fetchModel());
        } else if (originallyProducedEntity != null) {
            entity = originallyProducedEntity;
        } else {
            entity = producer.newEntity();
        }
        return entity;
    }

    /**
     * Constructs validation prototype having an <code>id</code>, <code>originallyProducedEntity</code> and <code>context</code> information.
     *
     * @param id -- validation prototype's identifier for retrieval from database or <code>null</code> if 'new' validation prototype is about to create
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this parameter contains originally produced entity instance not to produce it multiple times;
     *                                    it can also be intentionally <code>null</code> to trigger creation through producer
     * @param context -- for the case of <code>null</code> <code>id</code> and <code>null</code> <code>originallyProducedEntity</code>, this parameter contains the context from which the entity (validation prototype) will be produced
     * 
     * @return
     */
    public static <T extends AbstractEntity<?>> T createValidationPrototypeWithContext(
        final Long id,
        final T originallyProducedEntity,
        
        final CentreContext<T, AbstractEntity<?>> context,
        
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer
    ) {
        final DefaultEntityProducerWithContext<T> defProducer = (DefaultEntityProducerWithContext<T>) producer;
        defProducer.setContext(context);
        return createValidationPrototype(id, originallyProducedEntity, companion, defProducer);
    }
    
    ////////////////////////////////////// ENTITY CONSTRUCTION //////////////////////////////////////
    /**
     * Constructs validation prototype having an <code>id</code> and <code>originallyProducedEntity</code> information;
     * then applies <code>modifiedPropertiesHolder</code> against it.
     *
     * @param modifiedPropertiesHolder -- a set of properties with original and new values to be applied against validation prototype
     * @param id -- validation prototype's identifier for retrieval from database or <code>null</code> if 'new' validation prototype is about to create
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this parameter contains originally produced entity instance not to produce it multiple times;
     *                                    it can also be intentionally <code>null</code> to trigger creation through producer
     * 
     * @return -- applied validation prototype and <code>modifiedPropertiesHolder</code>
     */
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntity(
        final Map<String, Object> modifiedPropertiesHolder,
        final T originallyProducedEntity,
        
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer,
        final ICompanionObjectFinder companionFinder
    ) {
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");
        return applyModifHolder(modifiedPropertiesHolder, createValidationPrototype(id, originallyProducedEntity, companion, producer), companionFinder);
    }
    
    /**
     * Constructs validation prototype having an <code>id</code>, <code>originallyProducedEntity</code> and <code>context</code> information;
     * then applies <code>modifiedPropertiesHolder</code> against it.
     *
     * @param modifiedPropertiesHolder -- a set of properties with original and new values to be applied against validation prototype
     * @param id -- validation prototype's identifier for retrieval from database or <code>null</code> if 'new' validation prototype is about to create
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this parameter contains originally produced entity instance not to produce it multiple times;
     *                                    it can also be intentionally <code>null</code> to trigger creation through producer
     * @param context -- for the case of <code>null</code> <code>id</code> and <code>null</code> <code>originallyProducedEntity</code>, this parameter contains the context from which the entity (validation prototype) will be produced
     * 
     * @return -- applied validation prototype and <code>modifiedPropertiesHolder</code>
     */
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntityWithContext(
        final Map<String, Object> modifiedPropertiesHolder,
        final T originallyProducedEntity,
        
        final CentreContext<T, AbstractEntity<?>> context,
        
        final int tabCount,
        
        final IEntityDao<T> companion, 
        final IEntityProducer<T> producer,
        final ICompanionObjectFinder companionFinder
    ) {
        logger.debug(tabs(tabCount) + "constructEntity: started.");
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");
        final T validationPrototypeWithContext = createValidationPrototypeWithContext(id, originallyProducedEntity, context, companion, producer);
        logger.debug(tabs(tabCount) + "constructEntity: validationPrototypeWithContext.");
        final Pair<T, Map<String, Object>> constructed = applyModifHolder(modifiedPropertiesHolder, validationPrototypeWithContext, companionFinder);
        logger.debug(tabs(tabCount) + "constructEntity: finished.");
        return constructed;
    }
    
    /**
     * Applies <code>modifiedPropertiesHolder</code> against <code>validationPrototype</code>.
     * 
     * @param modifiedPropertiesHolder -- a set of properties with original and new values to be applied against validation prototype
     * @param validationPrototype
     * @param companionFinder
     * 
     * @return -- applied validation prototype and <code>modifiedPropertiesHolder</code>
     */
    private static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> applyModifHolder(
        final Map<String, Object> modifiedPropertiesHolder, 
        final M validationPrototype, 
        final ICompanionObjectFinder companionFinder
    ) {
        return new Pair<>(EntityResourceUtils.apply(modifiedPropertiesHolder, validationPrototype, companionFinder), modifiedPropertiesHolder);
    }
}
