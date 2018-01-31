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
 * A set of utilities for creating a validation prototype by either:
 * <ol>
 * <li>retrieving from a database,
 * <li>using an already produced instance, or 
 * <li>producing it with / without context.
 * </ol> 
 * Also, contains methods to fully construct entity -- application of <code>modifiedPropertiesHolder</code> included.
 * 
 * @author TG Team
 *
 */
public class EntityRestorationUtils {
    private static final Logger logger = Logger.getLogger(EntityRestorationUtils.class);
    
    ////////////////////////////////////// VALIDATION PROTOTYPE CREATION //////////////////////////////////////
    /**
     * Constructs a validation prototype having an <code>id</code> and the <code>originallyProducedEntity</code> information.
     *
     * @param id -- the validation prototype's identifier for retrieval from the database or <code>null</code> if a "new" validation prototype is about to be created.
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this argument contains the originally produced entity instance to avoid producing it multiple times;
     *                                    it can also be <code>null</code> intentionally to trigger the creation by a producer.
     * 
     * @return
     */
    public static <T extends AbstractEntity<?>> T createValidationPrototype(
            final Long id,
            final T originallyProducedEntity,
            final IEntityDao<T> companion, 
            final IEntityProducer<T> producer) {
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
     * Constructs a validation prototype having an <code>id</code>, <code>originallyProducedEntity</code> and the <code>context</code> information.
     *
     * @param id -- the validation prototype's identifier for retrieval from the database or <code>null</code> if a "new" validation prototype is about to be created.
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this parameter contains the originally produced entity instance to avoid producing it multiple times;
     *                                    it can also be <code>null</code> intentionally to trigger the creation by a producer.
     * @param context -- for the case of <code>null</code> <code>id</code> and <code>null</code> <code>originallyProducedEntity</code>, this parameter contains the context from which the entity (validation prototype) is be produced.
     * 
     * @return
     */
    public static <T extends AbstractEntity<?>> T createValidationPrototypeWithContext(
            final Long id,
            final T originallyProducedEntity,
            final CentreContext<T, AbstractEntity<?>> context,
            final IEntityDao<T> companion, 
            final IEntityProducer<T> producer) {
        final DefaultEntityProducerWithContext<T> defProducer = (DefaultEntityProducerWithContext<T>) producer;
        defProducer.setContext(context);
        return createValidationPrototype(id, originallyProducedEntity, companion, defProducer);
    }
    
    ////////////////////////////////////// ENTITY CONSTRUCTION //////////////////////////////////////
    /**
     * Constructs validation prototype having an <code>id</code> and the <code>originallyProducedEntity</code> information;
     * then applies <code>modifiedPropertiesHolder</code> to it.
     *
     * @param modifiedPropertiesHolder -- a set of properties with original and new values to be applied to the validation prototype
     * @param id -- the validation prototype's identifier for retrieval from the database or <code>null</code> if a "new" validation prototype is about to be created
     * @param originallyProducedEntity -- for a case of <code>null</code> <code>id</code>, this argument contains the originally produced entity instance to avoid producing it multiple times;
     *                                    it can also be <code>null</code> intentionally to trigger the creation by a producer
     * 
     * @return -- applied validation prototype and <code>modifiedPropertiesHolder</code>
     */
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntity(
            final Map<String, Object> modifiedPropertiesHolder,
            final T originallyProducedEntity,
            final IEntityDao<T> companion, 
            final IEntityProducer<T> producer,
            final ICompanionObjectFinder companionFinder) {
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");
        return applyModifHolder(modifiedPropertiesHolder, createValidationPrototype(id, originallyProducedEntity, companion, producer), companionFinder);
    }
    
    /**
     * Constructs validation prototype having an <code>id</code>, <code>originallyProducedEntity</code> and the <code>context</code> information;
     * then applies <code>modifiedPropertiesHolder</code> to it.
     *
     * @param modifiedPropertiesHolder -- a set of properties with original and new values to be applied against the validation prototype.
     * @param id -- the validation prototype's identifier for retrieval from the database or <code>null</code> if a "new" validation prototype is about to be created.
     * @param originallyProducedEntity -- for the case of <code>null</code> <code>id</code>, this parameter contains originally produced entity instance not to produce it multiple times;
     *                                    it can also be <code>null</code> intentionally to trigger the creation by a producer.
     * @param context -- for the case of <code>null</code> <code>id</code> and <code>null</code> <code>originallyProducedEntity</code>, this argument contains the context from which the entity (validation prototype) is to be produced.
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
            final ICompanionObjectFinder companionFinder) {
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
     * Applies <code>modifiedPropertiesHolder</code> to <code>validationPrototype</code>.
     * 
     * @param modifiedPropertiesHolder -- a set of properties with original and new values to be applied to the validation prototype
     * @param validationPrototype
     * @param companionFinder
     * 
     * @return -- the applied validation prototype and <code>modifiedPropertiesHolder</code>.
     */
    private static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> applyModifHolder(
            final Map<String, Object> modifiedPropertiesHolder, 
            final M validationPrototype, 
            final ICompanionObjectFinder companionFinder) {
        return new Pair<>(EntityResourceUtils.apply(modifiedPropertiesHolder, validationPrototype, companionFinder), modifiedPropertiesHolder);
    }
}
