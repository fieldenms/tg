package ua.com.fielden.platform.web.utils;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityProducingException;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyAssignmentErrorHandler;

import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.tabs;

/// A set of utilities for creating a validation prototype by either:
///
/// - Retrieving an entity from a database.
/// - Using an already produced instance.
/// - Producing it with or without context.
///
/// Also, contains methods to fully construct an entity instance, including the application of `modifiedPropertiesHolder`.
///
public class EntityRestorationUtils {
    private static final String ENTITY_NOT_FOUND = "Entity [%s] could not be found.";
    private static final Logger logger = getLogger(EntityRestorationUtils.class);
    
    ////////////////////////////////////// VALIDATION PROTOTYPE CREATION //////////////////////////////////////
    /// Finds entity by `id`.
    /// Ensures that a domain-driven application's [IFilter] is used if defined.
    /// Default [#getFetchProvider()] is used to construct a fetch model.
    ///
    ///
    /// @param id         an entity ID
    /// @param reader     [IEntityReader] for entity reading - instrumented or not depending on actual needs
    ///
    /// @throws Result an exception with message [#ENTITY_NOT_FOUND] is thrown if no entity could be found.
    ///
    public static <T extends AbstractEntity<?>> T findByIdWithFiltering(
            final Long id,
            final IEntityReader<T> reader)
    {
        return findByIdWithFiltering(id, reader, reader.getFetchProvider().fetchModel());
    }
    
    /// Finds entity by `id`.
    /// Ensures that a domain-driven application's [IFilter] is used if defined.
    ///
    /// @param id         an entity ID
    /// @param reader     [IEntityReader] for entity reading - instrumented or not depending on actual needs
    /// @param fetchModel a custom fetch model
    ///
    /// @throws Result an exception with message [#ENTITY_NOT_FOUND] is thrown if no entity could be found.
    ///
    public static <T extends AbstractEntity<?>> T findByIdWithFiltering(final Long id, final IEntityReader<T> reader, final fetch<T> fetchModel) {
        return findWithFiltering((filtered) -> reader.findById(filtered, id, fetchModel), reader);
    }
    
    /// Finds entity by `keyValues`.
    /// Ensures that a domain-driven application's [IFilter] is used if defined.
    /// Default [#getFetchProvider()] is used to construct a fetch model.
    ///
    /// @param reader    [IEntityReader] for entity reading - instrumented or not depending on actual needs
    /// @param keyValues an array of key values
    ///
    /// @throws Result an exception with message [#ENTITY_NOT_FOUND] is thrown if no entity could be found.
    ///
    public static <T extends AbstractEntity<?>> T findByKeyWithFiltering(
            final IEntityReader<T> reader,
            final Object... keyValues)
    {
        return findWithFiltering((filtered) -> reader.findByKeyAndFetch(filtered, reader.getFetchProvider().fetchModel(), keyValues), reader);
    }
    
    /// Finds entity using `finder`.
    /// Ensures that a domain-driven application's [IFilter] is used if defined.
    ///
    /// @param finder function with `filtered` argument to find an entity.
    /// @param reader [IEntityReader] for entity reading - instrumented or not depending on actual needs.
    ///
    /// @throws Result an exception with message [#ENTITY_NOT_FOUND] is thrown if no entity could be found.
    ///
    private static <T extends AbstractEntity<?>> T findWithFiltering(
            final Function<Boolean, T> finder,
            final IEntityReader<T> reader)
    {
        return ofNullable(finder.apply(true)).orElseThrow(() -> failure(format(ENTITY_NOT_FOUND, getEntityTitleAndDesc(reader.getEntityType()).getKey())));
    }

    /// Constructs a validation prototype based on entity `id` and `originallyProducedEntity`.
    ///
    /// @param id                       the validation prototype's identifier for retrieval from a database or `null`, if a "new" validation prototype is being created.
    /// @param originallyProducedEntity the originally produced entity instance to avoid producing it multiple times (if `id = null`);
    ///                                 it can be `null` intentionally to trigger the creation by a producer
    ///
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
            entity = findByIdWithFiltering(id, companion);
        } else if (originallyProducedEntity != null) {
            entity = originallyProducedEntity;
        } else {
            entity = producer.newEntity();
        }
        return entity;
    }

    /// Constructs a validation prototype based on entity `id`, `originallyProducedEntity`, and `context`.
    ///
    /// @param id                       the validation prototype's identifier for retrieval from a database or `null`, if a "new" validation prototype is being created.
    /// @param originallyProducedEntity the originally produced entity instance to avoid producing it multiple times (if `id = null`);
    ///                                 it can be `null` intentionally to trigger the creation by a producer
    /// @param context                  for the case of `id = null` and `originallyProducedEntity = null`,
    ///                                 this parameter contains the context from which the entity (validation prototype) can be produced.
    ///
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
    /// Creates a validation prototype based on entity `id` (carried in `modifiedPropertiesHolder`) and `originallyProducedEntity`.
    /// Applies `modifiedPropertiesHolder` to the validation prototype.
    ///
    /// The value of `id` in `modifiedPropertiesHolder` represents the validation prototype's identifier for the retrieval from a database.
    /// This value is `null` if a "new" validation prototype is being created.
    ///
    /// @param modifiedPropertiesHolder a set of properties with original and new values to be applied to the validation prototype
    /// @param originallyProducedEntity the originally produced entity instance to avoid producing it multiple times (if `modifiedPropertiesHolder.id = null`);
    ///                                 it can be `null` intentionally to trigger the creation by a producer
    ///
    /// @return the applied validation prototype and `modifiedPropertiesHolder`
    ///
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntity(
            final Map<String, Object> modifiedPropertiesHolder,
            final PropertyAssignmentErrorHandler propApplicationErrorHandler,
            final T originallyProducedEntity,
            final IEntityDao<T> companion, 
            final IEntityProducer<T> producer,
            final ICompanionObjectFinder companionFinder) {
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");
        return applyModifHolder(modifiedPropertiesHolder, propApplicationErrorHandler, createValidationPrototype(id, originallyProducedEntity, companion, producer), companionFinder);
    }

    /// Creates a validation prototype based on entity `id` (carried in `modifiedPropertiesHolder`), `originallyProducedEntity`, and `context`.
    /// Applies `modifiedPropertiesHolder` to the validation prototype.
    ///
    /// The value of `id` in `modifiedPropertiesHolder` represents the validation prototype's identifier for the retrieval from a database.
    /// This value is `null` if a "new" validation prototype is being created.
    ///
    /// @param modifiedPropertiesHolder    a set of properties with original and new values to be applied to the validation prototype
    /// @param propApplicationErrorHandler a handler for errors that occur during the application of `modifiedPropertiesHolder`
    /// @param originallyProducedEntity    the originally produced entity instance to avoid producing it multiple times (if `modifiedPropertiesHolder.id = null`);
    ///                                    it can be `null` intentionally to trigger the creation by a producer
    /// @param context                     for the case of `id = null` and `originallyProducedEntity = null`,
    ///                                    this argument contains the context from which the entity (validation prototype) can be produced.
    ///
    /// @return the applied validation prototype and `modifiedPropertiesHolder`
    ///
    public static <T extends AbstractEntity<?>> Pair<T, Map<String, Object>> constructEntityWithContext(
            final Map<String, Object> modifiedPropertiesHolder,
            final PropertyAssignmentErrorHandler propApplicationErrorHandler,
            final T originallyProducedEntity,
            final CentreContext<T, AbstractEntity<?>> context,
            final int tabCount,
            final IEntityDao<T> companion, 
            final IEntityProducer<T> producer,
            final ICompanionObjectFinder companionFinder) {
        logger.debug(() -> tabs(tabCount) + "constructEntity: started.");
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");
        final T validationPrototypeWithContext = createValidationPrototypeWithContext(id, originallyProducedEntity, context, companion, producer);
        logger.debug(() -> tabs(tabCount) + "constructEntity: validationPrototypeWithContext.");
        final Pair<T, Map<String, Object>> constructed = applyModifHolder(modifiedPropertiesHolder, propApplicationErrorHandler, validationPrototypeWithContext, companionFinder);
        logger.debug(() -> tabs(tabCount) + "constructEntity: finished.");
        return constructed;
    }
    
    /// Applies `modifiedPropertiesHolder` to `validationPrototype`.
    ///
    /// @param modifiedPropertiesHolder    a set of properties with original and new values to be applied to the validation prototype
    /// @param propApplicationErrorHandler a handler for errors that occur during the application of `modifiedPropertiesHolder`
    /// @param validationPrototype         a validation prototype to which `modifiedPropertiesHolder` is applied
    /// @param companionFinder             a companion finder for data retrieval
    ///
    /// @return the applied validation prototype and `modifiedPropertiesHolder`.
    private static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> applyModifHolder(
            final Map<String, Object> modifiedPropertiesHolder,
            final PropertyAssignmentErrorHandler propApplicationErrorHandler,
            final M validationPrototype, 
            final ICompanionObjectFinder companionFinder)
    {
        return new Pair<>(EntityResourceUtils.apply(modifiedPropertiesHolder, validationPrototype, propApplicationErrorHandler, companionFinder), modifiedPropertiesHolder);
    }
    
}
