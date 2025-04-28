package ua.com.fielden.platform.companion;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
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
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IUniversalConstants;

import javax.persistence.OptimisticLockException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import static ua.com.fielden.platform.entity.validation.custom.DefaultEntityValidator.validateWithoutCritOnly;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.*;
import static ua.com.fielden.platform.reflection.Reflector.isMethodOverriddenOrDeclared;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;
import static ua.com.fielden.platform.utils.EntityUtils.areEqual;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
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
        if (count == 1 && entity.getId().longValue() != ((Number) ids.get(0).get(AbstractEntity.ID)).longValue()) {
            throw new EntityAlreadyExists("%s [%s] already exists.".formatted(getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // load the entity directly from the session
        final T persistedEntity = (T) session.load(entity.getType(), entity.getId());
        persistedEntity.setIgnoreEditableState(true);
        // check for data staleness and try to resolve the conflict is possible (refer #83)
        if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion() && !canResolveConflict(entity, persistedEntity)) {
            throw new EntityCompanionException(format("%s %s [%s] could not be saved.", ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES, getEntityTitleAndDesc(entityType).getKey(), entity));
        }

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching ? empty() : (maybeFetch.isPresent() ? maybeFetch : of(FetchModelReconstructor.reconstruct(entity)));

        // proceed with property assignment from entity to persistent entity, which in case of a resolvable conflict acts like a fetch/rebase in git
        // it is essential that if a property is of an entity type it should be re-associated with the current session before being set
        // the easiest way to do that is to load entity by id using the current session
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            // set of meta-properties and set of properties with metadata may be different, but persistent properties
            // must always be present in both sets
            final Optional<PropertyMetadata> propMetadata = entityMetadata.property(prop).orElseThrow(Function.identity());
            if (propMetadata.filter(PropertyMetadata::isPersistent).isPresent()) {
                final Object value = prop.getValue();
                if (shouldProcessAsActivatable(entity, prop)) {
                    handleDirtyActivatableProperty(entity, persistedEntity, prop, value, session);
                } else if (value instanceof AbstractEntity && !(value instanceof PropertyDescriptor) && !(value instanceof AbstractUnionEntity)) {
                    persistedEntity.set(prop.getName(), session.load(((AbstractEntity<?>) value).getType(), ((AbstractEntity<?>) value).getId()));
                } else {
                    persistedEntity.set(prop.getName(), value);
                }
            }
        } // end of processing dirty properties

        // handle ref counts of non-dirty activatable properties
        if (entity instanceof ActivatableAbstractEntity) {
            handleNonDirtyActivatableIfNecessary(entity, persistedEntity, session);
        }

        // perform meta-data assignment to capture the information about this modification
        if (entity instanceof AbstractPersistentEntity) {
            assignLastModificationInfo((AbstractPersistentEntity<?>) entity, (AbstractPersistentEntity<?>) persistedEntity);
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

    /**
     * Handles dirty activatable property in a special way that manages refCount of its current and previous values, but only if the entity being saving is an activatable that does
     * not fall into the category of those with type that governs deactivatable dependency of the entity being saved.
     *
     * @param entity
     * @param persistedEntity
     * @param prop
     * @param value
     * @param session
     */
    private void handleDirtyActivatableProperty(final T entity, final T persistedEntity, final MetaProperty<?> prop, final Object value, final Session session) {
        final String propName = prop.getName();
        // dirty activatable handling only needs to be performed if the current and persisted values are different
        // at this stage of program execution, these values can only be the same iff a concurrent modification to the same value took place (i.e. non-conflicting concurrent change)
        // in such case recalculation of refCount for respective entities has already been performed, and double-dipping should be avoided
        if (equalsEx(value, persistedEntity.get(propName))) {
            return;
        }
        // if value is null then an activatable entity has been dereferenced and its refCount needs to be decremented
        // but only if the dereferenced value is an active activatable and the entity being saved is not being made active -- thus previously it was not counted as a reference
        if (value == null) {
            final MetaProperty<Boolean> activeProp = entity.getProperty(ACTIVE);
            final boolean beingActivated = activeProp.isDirty() && activeProp.getValue();
            // get the latest value of the dereferenced activatable as the current value of the persisted entity version from the database and decrement its ref count
            // original property value should not be null, otherwise property would not become dirty by assigning null
            final ActivatableAbstractEntity<?> origValue = (ActivatableAbstractEntity<?>) entity.getProperty(propName).getOriginalValue();
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.load(prop.getType(), origValue.getId(), UPGRADE);
            // if persistedValue active and does not equal to the entity being saving then need to decrement its refCount
            if (!beingActivated && persistedValue.isActive() && !areEqual(entity, persistedValue)) { // avoid counting self-references
                persistedValue.setIgnoreEditableState(true);
                session.update(persistedValue.decRefCount());
            }

            // assign null as the property value to actually dereference activatable
            persistedEntity.set(propName, null);
        } else { // otherwise, there could be either referencing (i.e. before property was null) or a reference change (i.e. from one value to some other)
            // need to process previous property value
            final AbstractEntity<?> origValue = (ActivatableAbstractEntity<?>) entity.getProperty(propName).getOriginalValue();
            if (origValue != null && !areEqual(entity, origValue)) { // need to decrement refCount for the dereferenced entity, but avoid counting self-references
                final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.load(prop.getType(), origValue.getId(), UPGRADE);
                persistedValue.setIgnoreEditableState(true);
                session.update(persistedValue.decRefCount());
            }
            // also need increment refCount for a newly referenced activatable
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.load(prop.getType(), ((AbstractEntity<?>) value).getId(), UPGRADE);
            if (!areEqual(entity, persistedValue)) { // avoid counting self-references
                // Now let's check if the entity itself is an active activatable instance.
                // This influences the decision to increment `refCount` for the newly referenced activatable.
                // Otherwise, there is no reason to increment `refCount` for the referenced instance.
                // In other words, an inactive entity does not count as an active referencer.
                if (entity.<Boolean>get(ACTIVE)) {
                    // If the referenced instance is not active (e.g. due to concurrent deactivation), then referencing is no longer relevant,
                    // and an exception should be thrown.
                    if (!persistedValue.isActive()) {
                        final String persistedEntityTitle = getEntityTitleAndDesc(persistedEntity.getType()).getKey();
                        final String persistedValueTitle = getEntityTitleAndDesc(persistedValue.getType()).getKey();
                        throw new EntityCompanionException("%s [%s] has a reference to already inactive %s [%s].".formatted(persistedEntityTitle, persistedEntity, persistedValueTitle, persistedValue));
                    }
                    else {
                        persistedValue.setIgnoreEditableState(true);
                        session.update(persistedValue.incRefCount());
                    }
                }
            }

            // assign updated activatable as the property value
            persistedEntity.set(propName, persistedValue);
        }
    }

    /**
     * In case an entity is activatable and has just been activated or deactivated, it is also necessary to make sure that all previously referenced activatables, which did not fall
     * into the dirty category and are active, get their refCount increment or decremented accordingly.
     *
     * @param entity
     * @param persistedEntity
     * @param session
     */
    private void handleNonDirtyActivatableIfNecessary(final T entity, final T persistedEntity, final Session session) {
        final MetaProperty<Boolean> activeProp = entity.getProperty(ACTIVE);
        // was activatable entity just activated?
        if (activeProp.isDirty()) {
            // let's collect activatable not dirty properties from entity to check them for activity and also to increment their refCount
            final Set<String> keyMembers = Finder.getKeyMembers(entity.getType()).stream().map(Field::getName).collect(Collectors.toSet());
            for (final T2<String, Class<ActivatableAbstractEntity<?>>> propNameAndType : collectActivatableNotDirtyProperties(entity, keyMembers)) {
                // get value from a persisted version of entity, which is loaded by Hibernate
                // if a corresponding property is proxied due to an insufficient fetch model, its value is retrieved lazily by Hibernate
                final AbstractEntity<?> value = persistedEntity.get(propNameAndType._1);
                if (value != null) { // if there is actually some value
                    // load activatable value
                    final ActivatableAbstractEntity<?> persistedValue = session.load(propNameAndType._2, value.getId(), UPGRADE);
                    persistedValue.setIgnoreEditableState(true);
                    // if activatable property value is not a self-reference,
                    // then need to check if it is active and if so increment its refCount.
                    // otherwise, if activatable is not active, then we've got an erroneous situation that should prevent activation of entity.
                    if (!areEqual(entity, persistedValue)) {
                        if (activeProp.getValue()) { // is entity being activated?
                            if (!persistedValue.isActive()) { // if activatable is not active then this is an error
                                final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
                                final String persistedValueTitle = getEntityTitleAndDesc(propNameAndType._2).getKey();
                                throw new EntityCompanionException(format("%s [%s] has a reference to already inactive %s [%s].", entityTitle, entity, persistedValueTitle, persistedValue));
                            } else { // otherwise, increment refCount
                                session.update(persistedValue.incRefCount());
                            }
                        } else if (persistedValue.isActive()) { // is entity being deactivated, but is referencing an active activatable?
                            session.update(persistedValue.decRefCount());
                        }
                    }
                }
            }

            // separately need to perform deactivation of deactivatable dependencies in case where the entity being saved is deactivated
            if (!activeProp.getValue()) {
                final List<? extends ActivatableAbstractEntity<?>> deactivatables = findActiveDeactivatableDependencies((ActivatableAbstractEntity<?>) entity, coFinder);
                for (final ActivatableAbstractEntity<?> deactivatable : deactivatables) {
                    deactivatable.set(ACTIVE, false);
                    final Result result = deactivatable.isValid();
                    if (result.isSuccessful()) {
                        // persisting of deactivatables should go through the logic of companion save
                        // and cannot be persisted by just using a call to Hibernate Session
                        final CommonEntityDao co = coFinder.find(deactivatable.getType());
                        co.save(deactivatable);
                    } else {
                        throw result;
                    }
                }
            }
        }
    }

    /**
     * This is a convenient predicate method that identifies whether the specified property needs to be processed as an activatable reference.
     *
     * @param entity
     * @param prop
     * @return
     */
    private boolean shouldProcessAsActivatable(final T entity, final MetaProperty<?> prop) {
        final boolean shouldProcessAsActivatable;
        if (prop.isActivatable() && entity instanceof ActivatableAbstractEntity && isNotSpecialActivatableToBeSkipped(prop)) {
            final Class<? extends ActivatableAbstractEntity<?>> propType = (Class<? extends ActivatableAbstractEntity<?>>) prop.getType();
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
                shouldProcessAsActivatable = !Set.of(ddAnnotation.value()).contains(entity.getType());
            } else {
                shouldProcessAsActivatable = true;
            }
        } else {
            shouldProcessAsActivatable = false;
        }
        return shouldProcessAsActivatable;
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
            final Set<String> keyMembers = Finder.getKeyMembers(entity.getType()).stream().map(Field::getName).collect(toSet());
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> activatableDirtyProperties = collectActivatableDirtyProperties(entity, keyMembers);

            for (final MetaProperty prop : activatableDirtyProperties) {
                if (prop.getValue() != null) {
                    // need to update refCount for the activatable entity
                    final ActivatableAbstractEntity<?> value = (ActivatableAbstractEntity<?>) prop.getValue();
                    final ActivatableAbstractEntity<?>  persistedEntity = (ActivatableAbstractEntity<?> ) session.load(value.getType(), value.getId(), UPGRADE);
                    // If the referenced instance is not active (e.g. due to concurrent deactivation), then referencing is no longer relevant,
                    // and an exception should be thrown.
                    if (!persistedEntity.isActive()) {
                        final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
                        final String persistedEntityTitle = getEntityTitleAndDesc(persistedEntity.getType()).getKey();
                        throw new EntityCompanionException("%s [%s] has a reference to already inactive %s [%s].".formatted(entityTitle, entity, persistedEntityTitle, persistedEntity));

                    }
                    persistedEntity.setIgnoreEditableState(true);
                    session.update(persistedEntity.incRefCount());
                }
            }
        }

        // depending on whether the current entity represents a one-2-one association or not, it may require a new ID
        // in case of one-2-one association, the value of ID is derived from its key's ID and does not need to be generated
        final boolean isOne2OneAssociation = AbstractEntity.class.isAssignableFrom(entity.getKeyType());
        final Long newEntityId = isOne2OneAssociation ? ((AbstractEntity<?>) entity.getKey()).getId() : nextIdValue(ID_SEQUENCE_NAME, session);
        try {
            final AbstractEntity<?> entityToSave = isOne2OneAssociation ? entity : entity.set(ID, newEntityId);
            session.save(entityToSave);
            session.flush(); // force saving to DB
            session.clear();
        } finally {
            // reset the value of ID to null for the passed-in entity to avoid any possible confusion stemming from the fact that entity became "persisted"
            // this is relevant for all entities, including one-2-one associations
            entity.set(ID, null);
        }
        
        return t2(newEntityId,
                  entityFetchOption.map(fetch -> findById.find(newEntityId, fetch, fillModel.get())).orElse(entity));
    }

    /**
     * Collects properties that represent dirty activatable entities that should have their ref counts updated.
     *
     * @param entity
     * @return
     */
    private Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> collectActivatableDirtyProperties(final T entity, final Set<String> keyMembers) {
        final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result = new HashSet<>();
        for (final MetaProperty<?> prop : entity.getProperties().values()) {
            if (prop.isDirty() && prop.isActivatable() && isNotSpecialActivatableToBeSkipped(prop)) {
                addToResultIfApplicableFromActivatablePerspective(entity, keyMembers, result, prop);
            }
        }
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
