package ua.com.fielden.platform.companion;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.joda.time.DateTime;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.EntityWasUpdatedOrDeletedConcurrently;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.IEntityFetcher;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.FillModelBuilder;
import ua.com.fielden.platform.entity.query.model.IFillModel;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IUniversalConstants;

import javax.persistence.OptimisticLockException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKey;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.model.IFillModel.emptyFillModel;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.ERR_INACTIVE_REFERENCES;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE;
import static ua.com.fielden.platform.entity.validation.custom.DefaultEntityValidator.validateWithoutCritOnly;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.Reflector.isMethodOverriddenOrDeclared;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;
import static ua.com.fielden.platform.utils.EntityUtils.areEqual;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.utils.MiscUtilities.optional;
import static ua.com.fielden.platform.utils.Validators.findActiveDeactivatableDependencies;

/**
 * The default implementation of contract {@link IEntityActuator} to save/update persistent entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public final class PersistentEntitySaver<T extends AbstractEntity<?>> implements IEntityActuator<T> {
    public static final String ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES = "Could not resolve conflicting changes.";
    public static final String ERR_OPTIMISTIC_LOCK = "%s [%s] was updated or deleted by another user. Please try saving again.";
    public static final String ERR_CONFLICTING_CONCURRENT_CHANGE = "There was a conflicting change by another user. Please try saving again.";
    // private static final String ERR_

    private final Supplier<Session> session;
    private final Supplier<String> transactionGuid;
    private final IDbVersionProvider dbVersionProvider;
    
    private final Class<T> entityType;
    private final Class<? extends Comparable<?>> keyType;
    private final ICompanionObjectFinder coFinder;
    private final IDomainMetadata domainMetadata;
    private final IEntityFetcher entityFetcher;
    private final IUserProvider userProvider;
    private final Supplier<DateTime> now;
    
    private final BiConsumer<T, Set<String>> processAfterSaveEvent;
    private final Consumer<MetaProperty<?>> assignBeforeSave;

    private final FindEntityById<T> findById;
    private final Function<EntityResultQueryModel<T>, Boolean> entityExists;

    private Boolean targetEntityTypeHasValidateOverridden;
    
    private final Logger logger;

    @Inject
    public PersistentEntitySaver(
            @Assisted final Supplier<Session> session,
            @Assisted final Supplier<String> transactionGuid,
            @Assisted final Class<T> entityType,
            @Assisted final Class<? extends Comparable<?>> keyType,
            @Assisted final BiConsumer<T, Set<String>> processAfterSaveEvent,
            @Assisted final Consumer<MetaProperty<?>> assignBeforeSave,
            @Assisted final FindEntityById<T> findById,
            @Assisted final Function<EntityResultQueryModel<T>, Boolean> entityExists,
            @Assisted final Logger logger,
            final IDbVersionProvider dbVersionProvider,
            final IEntityFetcher entityFetcher,
            final IUserProvider userProvider,
            final IUniversalConstants universalConstants,
            final ICompanionObjectFinder coFinder,
            final IDomainMetadata domainMetadata)
    {
        this.session = session;
        this.transactionGuid = transactionGuid;
        this.entityType = entityType;
        this.keyType = keyType;
        this.processAfterSaveEvent = processAfterSaveEvent;
        this.assignBeforeSave = assignBeforeSave;
        this.findById = findById;
        this.entityExists = entityExists;
        this.logger = logger;
        this.dbVersionProvider = dbVersionProvider;
        this.entityFetcher = entityFetcher;
        this.userProvider = userProvider;
        this.now = universalConstants::now;
        this.coFinder = coFinder;
        this.domainMetadata = domainMetadata;
    }

    @ImplementedBy(FactoryImpl.class)
    public interface IFactory {
        <E extends AbstractEntity<?>> PersistentEntitySaver<E> create(
                final Supplier<Session> session,
                final Supplier<String> transactionGuid,
                final Class<E> entityType,
                final Class<? extends Comparable<?>> keyType,
                final BiConsumer<E, Set<String>> processAfterSaveEvent,
                final Consumer<MetaProperty<?>> assignBeforeSave,
                final FindEntityById<E> findById,
                final Function<EntityResultQueryModel<E>, Boolean> entityExists,
                final Logger logger);
    }
    
    /**
     * Saves the provided entity. This method checks entity version and throws StaleObjectStateException if the provided entity is stale. There is no in-memory referential
     * integrity guarantee -- the returned instance is always a different instance. However, from the perspective of data loading, it is guaranteed that the object graph of the
     * returned instance contains the object graph of the passed in entity as its subgraph (i.e. it can be wider, but not narrower).
     * <p>
     * New or already persisted entity instances should not be reused after successful saving.
     * There is no guarantee as to what properties may or may not get mutated as part of the saving logic.
     * For example, saving new entities does not result in assigning their {@code ID} values to the passed in instances.
     * Instead, the returned instances should be used.
     * The future direction is complete immutability where setting any property value would not modify that entity, but return a new instance with the new property value assigned. 
     * <p>
     * This method must be invoked in the context of an open DB session and supports saving only of persistent entities. Otherwise, an exception is thrown. 
     */
    @Override
    public T save(final T entity) {
        return coreSave(entity, false, empty())._2;
    }

    public T2<Long, T> coreSave(final T entity, final boolean skipRefetching, final Optional<fetch<T>> maybeFetch) {
        if (entity == null || !entity.isPersistent()) {
            throw new EntityCompanionException(format("Only non-null persistent entities are permitted for saving. Ether type [%s] is not persistent or entity is null.", entityType.getName()));
        } else if (!entity.isInstrumented()) {
            throw new EntityCompanionException(format("Uninstrumented entity of type [%s] cannot be saved.", entityType.getName()));
        } else if (!entity.isDirty()) {
            final Result isValid = validateEntity(entity);
            if (isValid.isSuccessful()) {
                final T entityToReturn;
                if (skipRefetching) {
                    entityToReturn = entity;
                }
                else {
                    final fetch<T> fetchModel = maybeFetch.orElseGet(() -> FetchModelReconstructor.reconstruct(entity));
                    final var dm = domainMetadata.forEntity(entityType);
                    final var plainProps = dm.properties().stream().filter(PropertyMetadata::isPlain).collect(toSet());
                    if (plainProps.isEmpty()) {
                        entityToReturn = findById.find(entity.getId(), fetchModel, emptyFillModel());
                    }
                    else {
                        final var fillModelBld = new FillModelBuilder(domainMetadata);
                        plainProps.stream().forEach(dmp -> {
                            final var value = entity.get(dmp.name());
                            if (value != null) {
                                fillModelBld.set(dmp.name(), value);
                            }
                        });
                        entityToReturn = findById.find(entity.getId(), fetchModel, fillModelBld.build(entityType));
                    }
                }
                return t2(entity.getId(), entityToReturn);
            } else {
                throw isValid;
            }
        }

        // logger.debug(format("Start saving entity %s (ID = %s)", entity, entity.getId())); is taking too much time for many saves

        // Need to capture names of dirty properties before the actual saving takes place and makes all properties not dirty.
        // This is needed for executing after save event handler
        // Also, collect dirty plain properties to be used for a fill model to populate those properties after saving.
        final var dirtyPlainProps = new HashSet<MetaProperty<?>>();
        final var entityMetadata = domainMetadata.forEntity(entity.getType());
        final Set<String> dirtyPropNames = entity.getDirtyProperties().stream().map(mp -> {
            final var propName = mp.getName();
            if (entityMetadata.propertyOpt(propName).filter(PropertyMetadata::isPlain).isPresent()) {
                dirtyPlainProps.add(mp);
            }
            return propName;
        }).collect(toSet());

        final T2<Long, T> savedEntityAndId;
        // let's try to save entity
        try {
            // firstly validate the entity
            final Result isValid = validateEntity(entity);
            if (!isValid.isSuccessful()) {
                throw isValid;
            }
            final Supplier<IFillModel<T>> fillModel = () -> buildFillModel(dirtyPlainProps);
            // entity is valid, and we should proceed with saving
            // new and previously saved entities are handled differently
            if (!entity.isPersisted()) { // is it a new entity?
                savedEntityAndId = saveNewEntity(entity, skipRefetching, maybeFetch, fillModel, session.get());
            } else { // so, this is a modified entity
                savedEntityAndId = saveModifiedEntity(entity, skipRefetching, maybeFetch, fillModel, entityMetadata, session.get());
            }
        } finally {
            //logger.debug("Finished saving entity " + entity + " (ID = " + entity.getId() + ")");
        }

        final T savedEntity = savedEntityAndId._2;
        // this call never throws any exceptions
        processAfterSaveEvent.accept(savedEntity, dirtyPropNames);

        return savedEntityAndId;
    }

    /**
     * Builds a {@link IFillModel} based on {@code dirtyPlainProps} for a dirty entity, before it was saved.
     * <p>
     * The fill model is then used to restore values for dirty plain properties and reset their meta-state,
     * so that they are not dirty in the returned saved instance.
     *
     * @param dirtyPlainProps
     * @return
     */
    private IFillModel<T> buildFillModel(final Set<MetaProperty<?>> dirtyPlainProps) {
        if (dirtyPlainProps.isEmpty()) {
            return emptyFillModel();
        }
        final FillModelBuilder builder = new FillModelBuilder(domainMetadata);
        for (final MetaProperty<?> mp : dirtyPlainProps) {
            final var value = mp.getValue();
            if (value != null) {
                builder.set(mp.getName(), value);
            }
        }
        return builder.build(entityType);
    }

    /**
     * Chooses between overridden validation or an alternative default validation that skips crit-only properties. 
     * 
     * @param entity
     * @return
     */
    private Result validateEntity(final T entity) {
        if (targetEntityTypeHasValidateOverridden == null) {
            this.targetEntityTypeHasValidateOverridden = isMethodOverriddenOrDeclared(AbstractEntity.class, entityType, "validate");
        }
        return targetEntityTypeHasValidateOverridden ? entity.isValid() : entity.isValid(validateWithoutCritOnly);
    }

    /**
     * This is a helper method that is used during saving of the modified entity, which has been persisted previously, and ensures that no removal of required assignable before save properties has
     * happened.
     *
     * @param entity
     */
    private void checkDirtyMarkedForAssignmentBeforeSaveProperties(final T entity) {
        final List<MetaProperty<?>> props = entity.getDirtyProperties().stream().
                filter(p -> p.shouldAssignBeforeSave() && null != AnnotationReflector.getPropertyAnnotation(Required.class, entity.getType(), p.getName())).toList();
        if (!props.isEmpty()) {
            for (final MetaProperty<?> prop : props) {
                if (prop.getValue() == null) {
                    throw new EntityCompanionException("Property %s@%s is marked as assignable before save, but had its value removed.".formatted(prop.getName(), entity.getType().getName()));
                }
            }
        }
    }

    /**
     * Saves previously persisted and now modified entity.
     *
     * @param entity  an entity instance being saved
     * @param skipRefetching  instructs whether re-fetching should be skipped
     * @param maybeFetch  fetch model to apply to an entity instance after saving
     * @param fillModel  will be applied only in the presence of a fetch model
     * @param entityMetadata  entity domain metadata
     * @param session  the current database session
     */
    private T2<Long, T> saveModifiedEntity(
            final T entity,
            final boolean skipRefetching,
            final Optional<fetch<T>> maybeFetch,
            final Supplier<IFillModel<T>> fillModel,
            final EntityMetadata entityMetadata,
            final Session session)
    {
        // let's first prevent not permissibly modifications that could not be checked any earlier than this,
        // which pertain to required and marked as assign before save properties that must have values
        checkDirtyMarkedForAssignmentBeforeSaveProperties(entity);
        // let's make sure that entity is not a duplicate
        final AggregatedResultQueryModel model = select(createQueryByKey(dbVersionProvider.dbVersion(), entityType, keyType, false, entity.getKey())).yield().prop(AbstractEntity.ID).as(AbstractEntity.ID).modelAsAggregate();
        final List<EntityAggregates> ids = entityFetcher.getEntities(session, from(model).lightweight().model());
        final int count = ids.size();
        if (count == 1 && entity.getId().longValue() != ((Number) ids.getFirst().get(AbstractEntity.ID)).longValue()) {
            throw new EntityAlreadyExists("%s [%s] already exists.".formatted(getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // load the entity directly from the session
        final T persistedEntity = (T) session.load(entity.getType(), entity.getId());
        persistedEntity.setIgnoreEditableState(true);
        // check for data staleness and try to resolve the conflict is possible (refer #83)
        if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion() && !canResolveConflict(entity, persistedEntity)) {
            throw new EntityCompanionException(format("%s %s [%s] could not be saved.", ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES, getEntityTitleAndDesc(entityType).getKey(), entity));
        }

        // From this point on, until `session.flush()`, if the persisted version of `entity` is modified
        // (i.e., `persistedEntity.version` is no longer the actual persisted version),
        // then `session.flush()` will fail with StaleObjectException.
        // This may happen if and only if there is a concurrent update to the entity during this interval.

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching ? empty() : (maybeFetch.isPresent() ? maybeFetch : of(FetchModelReconstructor.reconstruct(entity)));

        // Need to record the persisted active status before `persistedEntity` is modified to support processing of deactivatable dependencies.
        final Optional<Boolean> persistedIsActive = entity instanceof ActivatableAbstractEntity ? optional(persistedEntity.get(ACTIVE)) : Optional.empty();

        try {
            refCountInstructions(entity, persistedEntity, session).forEach(ins -> execute(ins, session));
        } catch (final StaleStateException ex) {
            // StaleStateException may occur when a stale object is loaded from a session (via `session.load`).
            // For example, two entities concurrently begin referencing some other entity, thereby incrementing its `refCount` concurrently.
            // The exception occurs when a thread loads the modified entity for the second time, after its concurrent modification
            // (the first time it must have been loaded before its modification).
            throw new EntityCompanionException(ERR_CONFLICTING_CONCURRENT_CHANGE, ex);
        }

        // Proceed with property assignment from entity to persistent entity, which in case of a resolvable conflict acts like a fetch/rebase in Git.
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            // Set of meta-properties and set of properties with metadata may be different, but persistent properties
            // must always be present in both sets.
            final var maybePropMetadata = entityMetadata.property(prop).orElseThrow(Function.identity());
            if (maybePropMetadata.filter(PropertyMetadata::isPersistent).isPresent()) {
                final Object value = prop.getValue();
                // If a property is of an entity type, it should be re-associated with the current session before being set.
                // TODO Is this still true?
                if (value instanceof AbstractEntity<?> valueAsEntity && !(value instanceof PropertyDescriptor) && !(value instanceof AbstractUnionEntity)) {
                    persistedEntity.set(prop.getName(), session.load(valueAsEntity.getType(), valueAsEntity.getId()));
                }
                else {
                    persistedEntity.set(prop.getName(), value);
                }
            }
        }

        // Deactivatable dependencies need to be processed after property changes have been assigned to `persistedEntity`.
        processDeactivatableDependencies(entity, persistedEntity, persistedIsActive.orElse(false));

        // perform meta-data assignment to capture the information about this modification
        if (entity instanceof AbstractPersistentEntity<?> entityAsPersistent) {
            assignLastModificationInfo(entityAsPersistent, (AbstractPersistentEntity<?>) persistedEntity);
        }

        // update entity
        try {
            session.update(persistedEntity);
            session.flush();
        } catch (final OptimisticLockException ex) {
            // optimistic locking exception may occur during concurrent saving
            // let's present a more user-friendly message and log the error
            final String msg = ERR_OPTIMISTIC_LOCK.formatted(getEntityTitleAndDesc(persistedEntity).getKey(), persistedEntity);
            logger.error(msg, ex);
            throw new EntityWasUpdatedOrDeletedConcurrently(msg, ex);
        } finally {
            // We can only clear a session if the transaction is active. Otherwise, an exception is thrown.
            // However, we cannot simply check session.getTransaction().isActive() because Hibernate considers active transactions that are going to be rolled back.
            // And at the same time, Hibernate does not permit clearing a session for transactions that are marked to be rolled back.
            // Hence, the need to check transaction status directly.
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE) {
                session.clear();
            }
        }

        return t2(persistedEntity.getId(),
                  entityFetchOption.map(fetch -> findById.find(persistedEntity.getId(), fetch, fillModel.get())).orElse(persistedEntity));
    }

    /// If `entity` was deactivated, deactivates its [deactivatable dependencies][DeactivatableDependencies].
    ///
    private <E extends AbstractEntity<?>> void processDeactivatableDependencies(final E entity, final E persistedEntity, final boolean persistedIsActive) {
        if (entity instanceof ActivatableAbstractEntity<?> activatable) {
            processDeactivatableDependencies_(activatable, (ActivatableAbstractEntity<?>) persistedEntity, persistedIsActive);
        }
    }

    private <E extends ActivatableAbstractEntity<?>> void processDeactivatableDependencies_(final E entity, final E persistedEntity, final boolean persistedIsActive) {
        if (!entity.<Boolean>get(ACTIVE) && !entity.get(ACTIVE).equals(persistedIsActive)) {
            final List<? extends ActivatableAbstractEntity<?>> deactivatables = findActiveDeactivatableDependencies((ActivatableAbstractEntity<?>) entity, coFinder);
            for (final ActivatableAbstractEntity<?> deactivatable : deactivatables) {
                deactivatable.set(ACTIVE, false);
                deactivatable.isValid().ifFailure(Result::throwRuntime);
                // Persisting of deactivatables should go through the logic of companion save, they cannot be persisted through Hibernate Session directly.
                final CommonEntityDao co = coFinder.find(deactivatable.getType());
                co.save(deactivatable);
            }
        }
    }

    /// This predicate identifies whether the specified property needs to be processed as an activatable reference.
    ///
    /// @param entityType  entity type that contains `prop`
    ///
    @SuppressWarnings("unchecked")
    private boolean shouldProcessAsActivatable(final Class<? extends ActivatableAbstractEntity<?>> entityType, final MetaProperty<?> prop) {
        final boolean shouldProcessAsActivatable;
        if (prop.isActivatable() && isNotSpecialActivatableToBeSkipped(prop)) {
            final var propType = (Class<? extends AbstractEntity<?>>) prop.getType();
            final DeactivatableDependencies ddAnnotation = propType.getAnnotation(DeactivatableDependencies.class);
            if (ddAnnotation != null && prop.isKey()) {
                // If the type of the referencing property has deactivatable dependencies that include the type of the entity, which is being saved,
                // and the property is a key or a key member, then such property should be excluded from processing.
                //
                // Consider an example of activatable entity `Manager`, which has a key member `person: Person`.
                // Entity `Person` is activatable and includes `Manager` in its `@DeactivatableDependencies`.
                // Now imagine a scenario where an entity instance of `Manager` is being deactivated.
                // Property `Manager.person` would be considered for processing as it is of activatable type `Person`.
                // However, `Manager` is a specialisation of `Person`.
                // This is signified by the fact that `Manager.person` is a key member and `Person` includes `Manager` in its `@DeactivatableDependencies`.
                // Activation/deactivation of a `Manager` should not affect `refCount` for `Person`.
                // That is why, property `Manager.person` needs to be excluded from activatable processing.
                shouldProcessAsActivatable = !Set.of(ddAnnotation.value()).contains(entityType);
            } else {
                shouldProcessAsActivatable = true;
            }
        } else {
            shouldProcessAsActivatable = false;
        }
        return shouldProcessAsActivatable;
    }

    private static @Nullable ActivatableAbstractEntity<?> extractActivatable(final AbstractEntity<?> entity) {
        return switch (entity) {
            case ActivatableAbstractEntity<?> it -> it;
            case AbstractUnionEntity union -> union.activeEntity() instanceof ActivatableAbstractEntity<?> it ? it : null;
            case null, default -> null;
        };
    }

    /// Determines whether automatic conflict resolution between two entities of the same type is possible.
    /// This predicate is `true` iff all dirty properties of `entity` support automatic conflict resolution,
    /// and the value of each such property does not conflict with the same property of `persistedEntity`.
    /// The meaning of "conflict" has one common and one special case:
    /// 1. *Common case:* [EntityUtils#isConflicting] is used to identify a conflict for all properties and entities except property `active` for an activatable entity.
    /// 2. *Special case:* property `active` for an activatable entity is compared directly with the value of `active` in `persistentEntity`, and only if the dirty value is `false`.
    ///    This is required to avoid situations where an entity is being deactivated, and someone is creating an active reference to that instance concurrently.
    ///    Additionally, the condition `persistedEntity.refCount != 0` ensures that deactivation of a stale instance does not fail solely because the `entity` is stale.
    ///
    /// @see MapEntityTo#autoConflictResolution()
    /// @see MapTo#autoConflictResolution()
    ///
    private boolean canResolveConflict(final T entity, final T persistedEntity) {
        if (!requireNonNull(AnnotationReflector.getAnnotation(entity.getClass(), MapEntityTo.class)).autoConflictResolution()) {
            return false;
        }
        // Comparison of property values is most likely to trigger lazy loading if `persistedEntity` is a Hibernate proxy
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            final String name = prop.getName();
            final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, entity.getType(), name);
            if (mapTo != null && !mapTo.autoConflictResolution()) {
                return false;
            }
            final Object oldValue = prop.getOriginalValue();
            final Object newValue = prop.getValue();
            final Object persistedValue = persistedEntity.get(name);
            if (EntityUtils.isConflicting(newValue, oldValue, persistedValue) ||
                entity instanceof ActivatableAbstractEntity<?> && prop.getName().equals(ACTIVE) &&
                Boolean.FALSE.equals(newValue) && Boolean.TRUE.equals(persistedValue) &&
                Integer.valueOf(0).compareTo(persistedEntity.get(REF_COUNT)) != 0)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Persists an entity not persisted before.
     * Self-references are not possible for new entities simply because non-persisted instances are not permitted as property values.
     * Unless there is a special case of skipping entity exists validation, but then the developer would need to take care of that somehow specifically for each case.
     *
     * @param entity  an entity instance being saved for the first time
     * @param skipRefetching  instructs whether re-fetching should be skipped
     * @param maybeFetch  fetch model to apply to an entity instance after saving
     * @param fillModel  will be applied only in the presence of a fetch model
     * @param session  the current database session
     */
    private T2<Long, T> saveNewEntity(
            final T entity,
            final boolean skipRefetching,
            final Optional<fetch<T>> maybeFetch,
            final Supplier<IFillModel<T>> fillModel,
            final Session session)
    {
        // let's make sure that entity is not a duplicate
        if (entityExists.apply(createQueryByKey(dbVersionProvider.dbVersion(), entityType, keyType, false, entity.getKey()))) {
            throw new EntityAlreadyExists(format("%s [%s] already exists.", getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // process transactional assignments
        if (entity instanceof AbstractPersistentEntity) {
            assignCreationInfo((AbstractPersistentEntity<?>) entity);
        }
        assignPropertiesBeforeSave(entity);

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching ? empty() : (maybeFetch.isPresent() ? maybeFetch : of(FetchModelReconstructor.reconstruct(entity)));

        // new entity might be activatable, but this has no effect on its refCount -- should be zero as no other entity could yet reference it
        // however, it might reference other activatable entities, which warrants update to their refCount.
        final boolean shouldProcessActivatableProperties;
        if (entity instanceof ActivatableAbstractEntity) {
            final ActivatableAbstractEntity<?> activatable = (ActivatableAbstractEntity<?>) entity;
            shouldProcessActivatableProperties = activatable.isActive();
        } else { // refCount should not be updated if referenced by non-activatable
            shouldProcessActivatableProperties = false; // setting true would enable refCount update in case of saving non-activatable
        }

        if (shouldProcessActivatableProperties) {
            refCountInstructions(entity, null, session).forEach(ins -> execute(ins, session));
        }

        // Depending on whether the current entity represents a one-2-one association or not, it may require a new ID.
        // In the case of one-2-one association, the value of ID is derived from its key's ID and does not need to be generated.
        final boolean isOne2OneAssociation = AbstractEntity.class.isAssignableFrom(entity.getKeyType());
        final Long newEntityId = isOne2OneAssociation ? ((AbstractEntity<?>) entity.getKey()).getId() : nextIdValue(ID_SEQUENCE_NAME, session);
        try {
            final AbstractEntity<?> entityToSave = isOne2OneAssociation ? entity : entity.set(ID, newEntityId);
            session.save(entityToSave);
            session.flush(); // force saving to DB
            session.clear();
        } finally {
            // Reset the value of ID to null for the passed-in entity to avoid any possible confusion stemming from the fact that `entity` became persisted.
            // This is relevant for all entities, including one-2-one associations.
            entity.set(ID, null);
        }
        
        return t2(newEntityId,
                  entityFetchOption.map(fetch -> findById.find(newEntityId, fetch, fillModel.get())).orElse(entity));
    }

    /// Collects instructions for modifying `refCount` of activatable references from `entity`.
    ///
    /// @param entity           the activatable entity being saved
    /// @param persistedEntity  if `entity` is persisted, then this is its persisted version (Hibernate proxy)
    ///
    private <E extends AbstractEntity<?>>
    Stream<RefCountInstruction> refCountInstructions(final E entity, final @Nullable E persistedEntity, final Session session) {
        return entity instanceof ActivatableAbstractEntity<?> activatable
                ? refCountInstructions_(activatable, (ActivatableAbstractEntity<?>) persistedEntity, session)
                : Stream.of();
    }

    private <E extends ActivatableAbstractEntity<?>>
    Stream<RefCountInstruction> refCountInstructions_(final E entity, final @Nullable E persistedEntity, final Session session) {
        final var entityType = (Class<? extends ActivatableAbstractEntity<?>>) entity.getType();

        // If `entity` is persisted and active, process activatables among its non-dirty properties.
        //
        // Under ordinary conditions, where no concurrent modifications take place:
        // * If `entity` was deactivated, `refCount`s are decremented.
        // * If `entity` was activated, `refCount`s are incremented.
        //
        // If `entity` was concurrently modified, then these actions will be taken if and only if the persisted active status
        // is different from the active status in `entity`.
        // Otherwise, if persisted active status matches that of `entity`, then no action needs to be taken as all that work
        // would have already been done during the concurrent modification.
        final Stream<RefCountInstruction> instructionsForNonDirty;
        final MetaProperty<Boolean> activeProp = entity.getProperty(ACTIVE);
        if (persistedEntity != null && activeProp.isDirty() && !entity.get(ACTIVE).equals(persistedEntity.get(ACTIVE))) {
            instructionsForNonDirty = entity.getProperties().values()
                    .stream()
                    .filter(mp -> (mp.isProxy() || !mp.isDirty()) && shouldProcessAsActivatable(entityType, mp))
                    .<RefCountInstruction>map(mp -> {
                        final var propName = mp.getName();
                        // Get value from a persisted version of entity, which is loaded by Hibernate.
                        // If the property is proxied, its value will be retrieved lazily by Hibernate.
                        final var activatableValue = extractActivatable(persistedEntity.get(propName));
                        if (activatableValue != null) {
                            final var persistedActivatableValue = (ActivatableAbstractEntity<?>) session.load(activatableValue.getType(), activatableValue.getId(), UPGRADE);
                            persistedActivatableValue.setIgnoreEditableState(true);
                            // Update `refCount` if the referenced entity is active and is not a self-reference.
                            if (!areEqual(entity, persistedActivatableValue)) {
                                // If the entity being saved is active and references an inactive entity, then we have an erroneous situation
                                // and should prevent the activation of the entity being saved.
                                if (entity.get(ACTIVE)) {
                                    if (!persistedActivatableValue.isActive()) {
                                        session.detach(persistedActivatableValue);
                                        // This property may be proxied, thus we cannot use `mkInactiveReferenceFailure`.
                                        throw new EntityCompanionException(ERR_INACTIVE_REFERENCES.formatted(
                                                getTitleAndDesc(propName, entityType).getKey(),
                                                getEntityTitleAndDesc(entityType).getKey(),
                                                entity,
                                                getEntityTitleAndDesc(persistedActivatableValue).getKey(),
                                                persistedActivatableValue));
                                    }
                                    else {
                                        return new RefCountInstruction.Inc(persistedActivatableValue);
                                    }
                                }
                                else if (persistedActivatableValue.isActive()) {
                                    return new RefCountInstruction.Dec(persistedActivatableValue);
                                }
                                else {
                                    session.detach(persistedActivatableValue);
                                }
                            }
                            else {
                                session.detach(persistedActivatableValue);
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull);
        }
        else {
            instructionsForNonDirty = Stream.of();
        }

        final Stream<RefCountInstruction> instructionsForDirty = entity.getDirtyProperties()
                .stream()
                .filter(prop -> shouldProcessAsActivatable(entityType, prop))
                // If the current and persisted property values are the same, nothing needs to be done.
                // At this stage, these values can only be the same iff a non-conflicting concurrent modification occurred.
                // In such case, recalculation of `refCount` for the referenced entity has already been performed, and double-dipping should be avoided.
                .filter(prop -> !(persistedEntity != null && equalsEx(prop.getValue(), persistedEntity.get(prop.getName()))))
                .mapMulti((prop, sink) -> {
                    // If `entity` is persisted, the previous value of `prop`, if not null, was dereferenced, therefore its `refCount` needs to be decremented.
                    // Original property value should not be null, otherwise property would not become dirty by assigning null.
                    // `refCount` is decremented if and only if:
                    // * `persistedEntity` is active (otherwise, a concurrent update deactivated it and took care of decrementing `refCount`);
                    // * and the persisted version of the original value is active;
                    // * and the original value is not equal to the entity being saved (is not a self-reference).
                    final var originalActivatableValue = extractActivatable((AbstractEntity<?>) prop.getOriginalValue());
                    if (persistedEntity != null && originalActivatableValue != null) {
                        final var persistedValue = (ActivatableAbstractEntity<?>) session.load(originalActivatableValue.getType(), originalActivatableValue.getId(), UPGRADE);
                        if (persistedEntity.<Boolean>get(ACTIVE) && persistedValue.isActive() && !areEqual(entity, persistedValue)) {
                            sink.accept(new RefCountInstruction.Dec(originalActivatableValue));
                        }
                    }

                    final var activatableValue = extractActivatable((AbstractEntity<?>) prop.getValue());
                    // `entity` began referencing `activatableValue`.
                    if (activatableValue != null) {
                        // The use of `UPGRADE` is not strictly required for safe concurrent updates to `refCount`.
                        // However, with `UPGRADE`, `session.load()` acquires a pessimistic lock on the record,
                        // blocking other transactions from modifying it until the current transaction completes (commit or rollback).
                        // This guarantees that `session.load()` returns the most up-to-date persisted state.
                        //
                        // Without `UPGRADE`, multiple transactions can call `session.load()` concurrently without blocking.
                        // In this case, the first transaction to commit successfully updates the persistent state,
                        // while any concurrent transaction attempting to commit a conflicting change will fail with a `StaleObjectException`.
                        //
                        // Summary:
                        // * With `UPGRADE`: safe concurrent updates with blocking (pessimistic locking).
                        // * Without `UPGRADE`: safe concurrent updates without blocking (optimistic locking), but with a risk of rollback on conflict.
                        //
                        // We prefer using `UPGRADE` in this context to reduce the likelihood of rollbacks in potentially complex transactions caused by concurrent updates to `refCount`.
                        final var persistedActivatableValue = (ActivatableAbstractEntity<?>) session.load(activatableValue.getType(), activatableValue.getId(), UPGRADE);

                        // The newly referenced activatable `activatableValue` needs to have its `refCount` incremented if:
                        // * `entity` is active and was not concurrently deactivated OR `entity` is inactive and was concurrently activated;
                        // * and `activatableValue` is not a self-reference to `entity`;
                        // * and, if `entity` was concurrently modified, then its persisted version is still active.
                        //   If `entity` is concurrently deactivated, then it no longer affects `refCount` of `activatableValue`;
                        // * and the persisted version of `activatableValue` is active.
                        //   If `activatableValue` is concurrently deactivated, then we are in error -- active `entity` cannot reference inactive `activatableValue`.
                        if (!areEqual(entity, persistedActivatableValue)) {
                            if (persistedEntity == null
                                    ? entity.get(ACTIVE)
                                    : entity.getVersion() >= persistedEntity.getVersion() ? entity.get(ACTIVE) : persistedEntity.get(ACTIVE))
                            {
                                if (!persistedActivatableValue.isActive()) {
                                    session.detach(persistedActivatableValue);
                                    throw mkInactiveReferenceFailure(prop, persistedActivatableValue);
                                }
                                else {
                                    sink.accept(new RefCountInstruction.Inc(activatableValue));
                                }
                            }
                        }
                    }
                });

        return Stream.concat(instructionsForNonDirty, instructionsForDirty);
    }

    /// Instruction that operates on `refCount`.
    ///
    /// Execution is implemented by [#execute(RefCountInstruction, Session)].
    ///
    private sealed interface RefCountInstruction {
        /// Instruction that decrements `refCount`.
        record Dec(ActivatableAbstractEntity<?> entity) implements RefCountInstruction {}
        /// Instruction that increments `refCount`.
        record Inc(ActivatableAbstractEntity<?> entity) implements RefCountInstruction {}
    }

    private void execute(final RefCountInstruction instruction, final Session session) {
        switch (instruction) {
            case RefCountInstruction.Dec(var entity) -> {
                final var persistedEntity = (ActivatableAbstractEntity<?>) session.load(entity.getType(), entity.getId(), UPGRADE);
                persistedEntity.setIgnoreEditableState(true);
                session.update(persistedEntity.decRefCount());
            }
            case RefCountInstruction.Inc(var entity) -> {
                final var persistedEntity = (ActivatableAbstractEntity<?>) session.load(entity.getType(), entity.getId(), UPGRADE);
                persistedEntity.setIgnoreEditableState(true);
                session.update(persistedEntity.incRefCount());
            }
        }
    }

    /// Creates a failed validation result based on [EntityExistsValidator#ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE]
    /// with `referencedEntity` in the role of the inactive entity.
    /// This result is then associated with `mp` via [MetaProperty#setEntityExistsValidationResult(Result)], which fits into the standard validation lifecycle
    /// (if this result becomes outdated, it will be correctly replaced during revalidation).
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Result mkInactiveReferenceFailure(final MetaProperty mp, final ActivatableAbstractEntity<?> referencedEntity) {
        final var result = failure(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(referencedEntity.getType()).getKey(), referencedEntity));
        mp.setEntityExistsValidationResult(result);
        // `persistedEntity` may be proxied by Hibernate and cannot be serialised, hence cannot be set as the last invalid value.
        // For this reason we use a refetched instance.
        // NOTE: Sometimes `persistedEntity` itself is not proxied, but its contents are (values of entity-typed properties).
        final IEntityDao co = coFinder.find(referencedEntity.getType(), true /* uninstrumented */);
        final var refetched = co.findById(referencedEntity.getId(), FetchModelReconstructor.reconstruct(referencedEntity));
        mp.setLastInvalidValue(refetched);
        return result;
    }

    private void assignCreationInfo(final AbstractPersistentEntity<?> entity) {
        // unit tests utilise a permissive VIRTUAL_USER to persist a "current" user for the testing purposes
        // VIRTUAL_USER is transient and cannot be set as a value for properties of persistent entities
        // thus, a check for VIRTUAL_USER as a current user 
        if (!User.system_users.VIRTUAL_USER.name().equals(currUserOrException().getKey())) {
            entity.set(AbstractPersistentEntity.CREATED_BY, currUserOrException());
        }
        
        entity.set(AbstractPersistentEntity.CREATED_DATE, now.get().toDate());
        entity.set(AbstractPersistentEntity.CREATED_TRANSACTION_GUID, transactionGuid.get());
    }

    private void assignLastModificationInfo(final AbstractPersistentEntity<?> entity, final AbstractPersistentEntity<?> persistentEntity) {
        // if the entity is activatable and the only dirty property is refCount than there is no need to update the last-updated-by info
        if (entity instanceof ActivatableAbstractEntity) {
            final List<MetaProperty<?>> dirty = entity.getDirtyProperties();
            if (dirty.size() == 1 && ActivatableAbstractEntity.REF_COUNT.equals(dirty.get(0).getName())) {
                return;
            }
        }
        // unit tests utilise a permissive VIRTUAL_USER to persist a "current" user for the testing purposes.
        // VIRTUAL_USER is transient and cannot be set as a value for properties of persistent entities.
        // thus, a check for VIRTUAL_USER as a current user.
        if (!User.system_users.VIRTUAL_USER.name().equals(currUserOrException().getKey())) {
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_BY, currUserOrException());
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_DATE, now.get().toDate());
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID, transactionGuid.get());
        }
    }

    /**
     * Returns the current user if defined. Otherwise, it throws an exception.
     * @return
     */
    private User currUserOrException() {
        final User currUser = userProvider.getUser();
        if (currUser == null) {
            final String msg = "The current user is not defined.";
            logger.error(msg);
            throw new EntityCompanionException(msg);
        }
        return currUser;
    }
    /**
     * Assigns values to all properties marked for assignment before save. This method should be used only during the saving of new entities.
     *
     * @param entity
     */
    private void assignPropertiesBeforeSave(final T entity) {
        final List<MetaProperty<?>> props = entity.getProperties().values().stream().
                filter(MetaProperty::shouldAssignBeforeSave).collect(Collectors.toList());
        if (!props.isEmpty()) {
            final DateTime rightNow = now.get();
            if (rightNow == null) {
                throw new EntityCompanionException("The now() constant has not been assigned!");
            }

            for (final MetaProperty<?> prop : props) {
                final Object value = prop.getValue();
                if (value == null) {
                    if (User.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(currUserOrException());
                    } else if (Date.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(rightNow.toDate());
                    } else if (DateTime.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(rightNow);
                    } else {
                        assignBeforeSave.accept(prop);
                    }

                    if (prop.getValue() == null) {
                        throw new EntityCompanionException("Property %s@%s is marked as assignable before save, but no value could be determined.".formatted(prop.getName(), entity.getType().getName()));
                    }
                }
            }
        }
    }

    // This factory must be implemented by hand since com.google.inject.assistedinject.FactoryModuleBuilder
    // does not support generic factory methods.
    static final class FactoryImpl implements IFactory {
        private final IDbVersionProvider dbVersionProvider;
        private final IEntityFetcher entityFetcher;
        private final IUserProvider userProvider;
        private final IUniversalConstants universalConstants;
        private final ICompanionObjectFinder coFinder;
        private final IDomainMetadata domainMetadata;

        @Inject
        FactoryImpl(final IDbVersionProvider dbVersionProvider,
                    final IEntityFetcher entityFetcher,
                    final IUserProvider userProvider,
                    final IUniversalConstants universalConstants,
                    final ICompanionObjectFinder coFinder,
                    final IDomainMetadata domainMetadata) {
            this.dbVersionProvider = dbVersionProvider;
            this.entityFetcher = entityFetcher;
            this.userProvider = userProvider;
            this.universalConstants = universalConstants;
            this.coFinder = coFinder;
            this.domainMetadata = domainMetadata;
        }

        public <E extends AbstractEntity<?>> PersistentEntitySaver<E> create(
                final Supplier<Session> session,
                final Supplier<String> transactionGuid,
                final Class<E> entityType,
                final Class<? extends Comparable<?>> keyType,
                final BiConsumer<E, Set<String>> processAfterSaveEvent,
                final Consumer<MetaProperty<?>> assignBeforeSave,
                final FindEntityById<E> findById,
                final Function<EntityResultQueryModel<E>, Boolean> entityExists,
                final Logger logger)
        {
            return new PersistentEntitySaver<>(session, transactionGuid, entityType, keyType, processAfterSaveEvent,
                                               assignBeforeSave, findById, entityExists, logger,
                                               dbVersionProvider, entityFetcher, userProvider, universalConstants,
                                               coFinder, domainMetadata);
        }
    }

    @FunctionalInterface
    public interface FindEntityById<E extends AbstractEntity<?>> {

        E find(Long id, fetch<E> fetchModel, IFillModel<E> fillModel);

    }

}
