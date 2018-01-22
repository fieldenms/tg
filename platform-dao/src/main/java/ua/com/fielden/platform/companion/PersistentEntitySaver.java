package ua.com.fielden.platform.companion;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKey;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.addToResultIfApplicableFromActivatablePerspective;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.collectActivatableNotDirtyProperties;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;
import static ua.com.fielden.platform.utils.Validators.findActiveDeactivatableDependencies;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * The default implementation of contract {@link IEntityActuator} to save/update persistent entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public final class PersistentEntitySaver<T extends AbstractEntity<?>> implements IEntityActuator<T> {

    private final Supplier<Session> session;
    private final Supplier<String> transactionGuid;
    
    private final Class<T> entityType;
    private final Class<? extends Comparable<?>> keyType;
    private final Supplier<Boolean> isFilterable;
    private final Supplier<ICompanionObjectFinder> coFinder;
    private final Supplier<QueryExecutionContext> newQueryExecutionContext;
    private final Supplier<User> user;
    private final Supplier<DateTime> now;
    private final Supplier<Boolean> skipRefetching;
    
    private final BiConsumer<T, List<String>> processAfterSaveEvent;
    private final Consumer<MetaProperty<?>> assignBeforeSave;

    private final BiFunction<Long, fetch<T>, T> findById;
    private final Function<EntityResultQueryModel<T>, Integer> kount;

    private final Logger logger;
    
    public PersistentEntitySaver(
            final Supplier<Session> session,
            final Supplier<String> transactionGuid,
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final Supplier<User> user,
            final Supplier<DateTime> now,
            final Supplier<Boolean> skipRefetching,
            final Supplier<Boolean> isFilterable,
            final Supplier<ICompanionObjectFinder> coFinder,
            final Supplier<QueryExecutionContext> newQueryExecutionContext,
            final BiConsumer<T, List<String>> processAfterSaveEvent,
            final Consumer<MetaProperty<?>> assignBeforeSave,
            final BiFunction<Long, fetch<T>, T> findById,
            final Function<EntityResultQueryModel<T>, Integer> count,
            final Logger logger
            ) {
        this.session = session;
        this.transactionGuid = transactionGuid;
        this.entityType = entityType;
        this.keyType = keyType;
        this.user = user;
        this.now = now;
        this.skipRefetching = skipRefetching;
        this.isFilterable = isFilterable;
        this.coFinder = coFinder;
        this.newQueryExecutionContext = newQueryExecutionContext;
        
        this.processAfterSaveEvent = processAfterSaveEvent;
        this.assignBeforeSave = assignBeforeSave;
        
        this.findById = findById;
        this.kount = count;
        this.logger = logger;
    }
    
    /**
     * Saves the provided entity. This method checks entity version and throws StaleObjectStateException if the provided entity is stale. There is no in-memory referential
     * integrity guarantee -- the returned instance is always a different instance. However, from the perspective of data loading, it is guaranteed that the object graph of the
     * returned instance contains the object graph of the passed in entity as its subgraph (i.e. it can be wider, but not narrower).
     * <p>
     * This method must be invoked in the context of an open DB session and supports saving only of persistent entities. Otherwise, an exception is thrown. 
     */
    @Override
    public T save(final T entity) {
        if (entity == null || !entity.isPersistent()) {
            throw new EntityCompanionException(format("Only non-null persistent entities are permitted for saving. Ether type [%s] is not persistent or entity is null.", entityType.getName()));
        } else if (!entity.isInstrumented()) {
            throw new EntityCompanionException(format("Uninstrumented entity of type [%s] cannot be saved.", entityType.getName()));
        } else if (!entity.isDirty() && entity.isValid().isSuccessful()) {
            logger.debug(format("Entity [%s] is not dirty (ID = %s). Saving is skipped. Entity refetched.", entity, entity.getId()));
            return skipRefetching.get() ? entity : findById.apply(entity.getId(), FetchModelReconstructor.reconstruct(entity));
        }
        logger.debug(format("Start saving entity %s (ID = %s)", entity, entity.getId()));

        // need to capture names of dirty properties before the actual saving takes place and makes all properties not dirty
        // this is needed for executing after save event handler
        final List<String> dirtyProperties = entity.getDirtyProperties().stream().map(p -> p.getName()).collect(toList());

        final T resultantEntity;
        // let's try to save entity
        try {
            // firstly validate the entity
            final Result isValid = entity.isValid();
            if (!isValid.isSuccessful()) {
                throw isValid;
            }
            // entity is valid and we should proceed with saving
            // new and previously saved entities are handled differently
            if (!entity.isPersisted()) { // is it a new entity?
                resultantEntity = saveNewEntity(entity);
            } else { // so, this is a modified entity
                resultantEntity = saveModifiedEntity(entity);
            }
        } finally {
            logger.debug("Finished saving entity " + entity + " (ID = " + entity.getId() + ")");
        }

        // this call never throws any exceptions
        processAfterSaveEvent.accept(resultantEntity, dirtyProperties);

        return resultantEntity;
    }

    /**
     * This is a helper method that is used during saving of the modified entity, which has been persisted previously, and ensures that no removal of required assignable before save properties has
     * happened.
     *
     * @param entity
     */
    private void checkDirtyMarkedForAssignmentBeforeSaveProperties(final T entity) {
        final List<MetaProperty<?>> props = entity.getDirtyProperties().stream().
                filter(p -> p.shouldAssignBeforeSave() && null != AnnotationReflector.getPropertyAnnotation(Required.class, entity.getType(), p.getName())).
                collect(Collectors.toList());
        if (!props.isEmpty()) {
            for (final MetaProperty<?> prop : props) {
                if (prop.getValue() == null) {
                    throw new EntityCompanionException(format("Property %s@%s is marked as assignable before save, but had its value removed.", prop.getName(), entity.getType().getName()));
                }
            }
        }
    }

    /**
     * Saves previously persisted and now modified entity.
     *
     * @param entity
     */
    private T saveModifiedEntity(final T entity) {
        // let's first prevent not permissibly modifications that could not be checked any earlier than this,
        // which pertain to required and marked as assign before save properties that must have values
        checkDirtyMarkedForAssignmentBeforeSaveProperties(entity);
        // let's make sure that entity is not a duplicate
        final AggregatedResultQueryModel model = select(createQueryByKey(entityType, keyType, isFilterable.get(), entity.getKey())).yield().prop(AbstractEntity.ID).as(AbstractEntity.ID).modelAsAggregate();
        final QueryExecutionContext queryExecutionContext = newQueryExecutionContext.get();
        final List<EntityAggregates> ids = new EntityFetcher(queryExecutionContext).getEntities(from(model).lightweight().model());
        final int count = ids.size();
        if (count == 1 && entity.getId().longValue() != ((Number) ids.get(0).get(AbstractEntity.ID)).longValue()) {
            throw new EntityCompanionException(format("%s [%s] already exists.", TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // load the entity directly from the session
        final T persistedEntity = (T) session.get().load(entity.getType(), entity.getId());
        persistedEntity.setIgnoreEditableState(true);
        // check for data staleness and try to resolve the conflict is possible (refer #83)
        if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion() && !canResolveConflict(entity, persistedEntity)) {
            throw new EntityCompanionException(format("Could not resolve conflicting changes. %s [%s] could not be saved.", TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey(), entity));
        }

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching.get() ? Optional.empty() : Optional.of(FetchModelReconstructor.reconstruct(entity));


        // proceed with property assignment from entity to persistent entity, which in case of a resolvable conflict acts like a fetch/rebase in git
        // it is essential that if a property is of an entity type it should be re-associated with the current session before being set
        // the easiest way to do that is to load entity by id using the current session
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            final Object value = prop.getValue();
            if (shouldProcessAsActivatable(entity, prop)) {
                handleDirtyActivatableProperty(entity, persistedEntity, prop, value);
            } else if (value instanceof AbstractEntity && !(value instanceof PropertyDescriptor) && !(value instanceof AbstractUnionEntity)) {
                persistedEntity.set(prop.getName(), session.get().load(((AbstractEntity<?>) value).getType(), ((AbstractEntity<?>) value).getId()));
            } else {
                persistedEntity.set(prop.getName(), value);
            }
        } // end of processing dirty properties

        // handle ref counts of non-dirty activatable properties
        if (entity instanceof ActivatableAbstractEntity) {
            handleNonDirtyActivatableIfNecessary(entity, persistedEntity);
        }

        // perform meta-data assignment to capture the information about this modification
        if (entity instanceof AbstractPersistentEntity) {
            assignLastModificationInfo((AbstractPersistentEntity<?>) entity, (AbstractPersistentEntity<?>) persistedEntity);
        }

        // update entity
        session.get().update(persistedEntity);
        session.get().flush();
        session.get().clear();

        return entityFetchOption.map(fetch -> findById.apply(persistedEntity.getId(), fetch)).orElse(persistedEntity);
    }

    /**
     * Handles dirty activatable property in a special way that manages refCount of its current and previous values, but only if the entity being saving is an activatable that does
     * not fall into the category of those with type that governs deactivatable dependency of the entity being saved.
     *
     * @param entity
     * @param persistedEntity
     * @param prop
     * @param value
     */
    private void handleDirtyActivatableProperty(final T entity, final T persistedEntity, final MetaProperty<?> prop, final Object value) {
        final String propName = prop.getName();
        // if value is null then an activatable entity has been dereferenced and its refCount needs to be decremented
        // but only if the dereferenced value is an active activatable and the entity being saved is not being made active -- thus previously it was not counted as a reference
        if (value == null) {
            final MetaProperty<Boolean> activeProp = entity.getProperty(ACTIVE);
            final boolean beingActivated = activeProp.isDirty() && activeProp.getValue();
            // get the latest value of the dereferenced activatable as the current value of the persisted entity version from the database and decrement its ref count
            // previous property value should not be null as it would become dirty, also, there was no property conflict, so it can be safely assumed that previous value is NOT null
            final ActivatableAbstractEntity<?> prevValue = (ActivatableAbstractEntity<?>) entity.getProperty(propName).getPrevValue();
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.get().load(prop.getType(), prevValue.getId(), UPGRADE);
            // if persistedValue active and does not equal to the entity being saving then need to decrement its refCount
            if (!beingActivated && persistedValue.isActive() && !entity.equals(persistedValue)) { // avoid counting self-references
                persistedValue.setIgnoreEditableState(true);
                session.get().update(persistedValue.decRefCount());
            }

            // assign null as the property value to actually dereference activatable
            persistedEntity.set(propName, null);
        } else { // otherwise there could be either referencing (i.e. before property was null) or a reference change (i.e. from one value to some other)
            // need to process previous property value
            final AbstractEntity<?> prevValue = (ActivatableAbstractEntity<?>) entity.getProperty(propName).getPrevValue();
            if (prevValue != null && !entity.equals(prevValue)) { // need to decrement refCount for the dereferenced entity, but avoid counting self-references
                final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.get().load(prop.getType(), prevValue.getId(), UPGRADE);
                persistedValue.setIgnoreEditableState(true);
                session.get().update(persistedValue.decRefCount());
            }
            // also need increment refCount for a newly referenced activatable
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.get().load(prop.getType(), ((AbstractEntity<?>) value).getId(), UPGRADE);
            if (!entity.equals(persistedValue)) { // avoid counting self-references
                // now let's check if the entity itself is an active activatable
                // as this influences the decision to increment refCount for the newly referenced activatable
                // because, if it's not then there is no reason to increment refCout for the referenced instance
                // in other words, inactive entity does not count as an active referencer
                if (entity.<Boolean>get(ACTIVE)) {
                    persistedValue.setIgnoreEditableState(true);
                    session.get().update(persistedValue.incRefCount());
                }
            }

            // assign updated activatable as the property value
            persistedEntity.set(propName, persistedValue);
        }
    }

    /**
     * In case entity is activatable and has just been activated or deactivated, it is also necessary to make sure that all previously referenced activatables, which did not fall
     * into the dirty category and are active get their refCount increment or decremented accordingly
     *
     * @param entity
     * @param persistedEntity
     */
    private void handleNonDirtyActivatableIfNecessary(final T entity, final T persistedEntity) {
        final MetaProperty<Boolean> activeProp = entity.getProperty(ACTIVE);
        // was activatable entity just activated?
        if (activeProp.isDirty()) {
            // let's collect activatable not dirty properties from entity to check them for activity and also to increment their refCount
            final Set<String> keyMembers = Finder.getKeyMembers(entity.getType()).stream().map(f -> f.getName()).collect(Collectors.toSet());
            for (final T2<String, Class<ActivatableAbstractEntity<?>>> propNameAndType : collectActivatableNotDirtyProperties(entity, keyMembers)) {
                // get value from a persisted version of entity, which is loaded by Hibernate
                // if a corresponding property is proxied due to insufficient fetch model, its value is retrieved lazily by Hibernate
                final AbstractEntity<?> value = persistedEntity.get(propNameAndType._1);
                if (value != null) { // if there is actually some value
                    // load activatable value
                    final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) session.get().load(propNameAndType._2, value.getId(), UPGRADE);
                    persistedValue.setIgnoreEditableState(true);
                    // if activatable property value is not a self-reference
                    // then need to check if it is active and if so increment its refCount
                    // otherwise, if activatable is not active then we've got an erroneous situation that should prevent activation of entity
                    if (!entity.equals(persistedValue)) {
                        if (activeProp.getValue()) { // is entity being activated?
                            if (!persistedValue.isActive()) { // if activatable is not active then this is an error
                                final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
                                final String persistedValueTitle = TitlesDescsGetter.getEntityTitleAndDesc(propNameAndType._2).getKey();
                                throw new EntityCompanionException(format("%s [%s] has a reference to already inactive %s [%s].", entityTitle, entity, persistedValueTitle, persistedValue));
                            } else { // otherwise, increment refCount
                                session.get().update(persistedValue.incRefCount());
                            }
                        } else if (persistedValue.isActive()) { // is entity being deactivated, but is referencing an active activatable?
                            session.get().update(persistedValue.decRefCount());
                        }
                    }
                }
            }

            // separately need to perform deactivation of deactivatable dependencies in case where the entity being saved is deactivated
            if (!activeProp.getValue()) {
                final List<? extends ActivatableAbstractEntity<?>> deactivatables = findActiveDeactivatableDependencies((ActivatableAbstractEntity<?>) entity, coFinder.get());
                for (final ActivatableAbstractEntity<?> deactivatable : deactivatables) {
                    deactivatable.set(ACTIVE, false);
                    final Result result = deactivatable.isValid();
                    if (result.isSuccessful()) {
                        // persisting of deactivatables should go through the logic of companion save
                        // and cannot be persisted by just using a call to Hibernate Session
                        final CommonEntityDao co = coFinder.get().find(deactivatable.getType());
                        co.save(deactivatable);
                    } else {
                        throw result;
                    }
                }
            }
        }
    }

    /**
     * This is a convenient predicate method that identifies whether the specified property need to be processed as an activatable reference.
     *
     * @param entity
     * @param prop
     * @return
     */
    private boolean shouldProcessAsActivatable(final T entity, final MetaProperty<?> prop) {
        boolean shouldProcessAsActivatable;
        if (prop.isActivatable() && entity instanceof ActivatableAbstractEntity && isNotSpecialActivatableToBeSkipped(prop)) {
            final Class<? extends ActivatableAbstractEntity<?>> type = (Class<? extends ActivatableAbstractEntity<?>>) prop.getType();
            final DeactivatableDependencies ddAnnotation = type.getAnnotation(DeactivatableDependencies.class);
            if (ddAnnotation != null && prop.isKey()) {
                shouldProcessAsActivatable = !Arrays.asList(ddAnnotation.value()).contains(entity.getType());
            } else {
                shouldProcessAsActivatable = true;
            }
        } else {
            shouldProcessAsActivatable = false;
        }
        return shouldProcessAsActivatable;
    }

    /**
     * Determines whether automatic conflict resolves between the two entity instance is possible. The ability to resolve conflict automatically is based strictly on dirty
     * properties -- if dirty properties in <code>entity</code> are equals to the same properties in <code>persistedEntity</code> then the conflict can be resolved.
     *
     * @param entity
     * @param persistedEntity
     * @return
     */
    private boolean canResolveConflict(final T entity, final T persistedEntity) {
        // comparison of property values is most likely to trigger lazy loading
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            final String name = prop.getName();
            final Object oldValue = prop.getOriginalValue();
            final Object newValue = prop.getValue();
            final Object persistedValue = persistedEntity.get(name);
            if (EntityUtils.isConflicting(newValue, oldValue, persistedValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Persists an entity that was not persisted before. Self-references are not possible for new entities simply because non-persisted instances are not permitted as property
     * values. Unless there is a special case of skipping entity exists validation, but then the developer would need to take case of that somehow specifically for each specific
     * case.
     *
     * @param entity
     */
    private T saveNewEntity(final T entity) {
        // let's make sure that entity is not a duplicate
        
        final Integer count = kount.apply(createQueryByKey(entityType, keyType, isFilterable.get(), entity.getKey()));
        if (count > 0) {
            throw new EntityCompanionException(format("%s [%s] already exists.", TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // process transactional assignments
        if (entity instanceof AbstractPersistentEntity) {
            assignCreationInfo((AbstractPersistentEntity<?>) entity);
        }
        assignPropertiesBeforeSave(entity);
        
        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching.get() ? Optional.empty() : Optional.of(FetchModelReconstructor.reconstruct(entity));

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
            final Set<String> keyMembers = Finder.getKeyMembers(entity.getType()).stream().map(f -> f.getName()).collect(Collectors.toSet());
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> activatableDirtyProperties = collectActivatableDirtyProperties(entity, keyMembers);

            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : activatableDirtyProperties) {
                if (prop.getValue() != null) {
                    // need to update refCount for the activatable entity
                    final ActivatableAbstractEntity<?> value = prop.getValue();
                    final ActivatableAbstractEntity<?>  persistedEntity = (ActivatableAbstractEntity<?> ) session.get().load(value.getType(), value.getId(), UPGRADE);
                    // the returned value could already be inactive due to some concurrent modification
                    // therefore it is critical to ensure that the property of the current entity being saved can still accept the obtained value if it is inactive
                    if (!persistedEntity.isActive()) {
                        entity.beginInitialising();
                        entity.set(prop.getName(), persistedEntity);
                        entity.endInitialising();
                        
                        final Result res = prop.revalidate(false);
                        if (!res.isSuccessful()) {
                            throw res;
                        }
                    }
                    persistedEntity.setIgnoreEditableState(true);
                    session.get().update(persistedEntity.incRefCount());
                }
            }
        }

        // save the entity
        session.get().save(entity.set(ID, nextIdValue(ID_SEQUENCE_NAME, session.get())));
        session.get().flush();
        session.get().clear();

        return entityFetchOption.map(fetch -> findById.apply(entity.getId(), fetch)).orElse(entity);
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
        if (!User.system_users.VIRTUAL_USER.name().equals(user.get().getKey())) {
            entity.set(AbstractPersistentEntity.CREATED_BY, user.get());
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
        // unit tests utilise a permissive VIRTUAL_USER to persist a "current" user for the testing purposes
        // VIRTUAL_USER is transient and cannot be set as a value for properties of persistent entities
        // thus, a check for VIRTUAL_USER as a current user 
        if (!User.system_users.VIRTUAL_USER.name().equals(user.get().getKey())) {
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_BY, user.get());
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_DATE, now.get().toDate());
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID, transactionGuid.get());
        }
    }
    
    /**
     * Assigns values to all properties marked for assignment before save. This method should be used only during saving of new entities.
     *
     * @param entity
     */
    private void assignPropertiesBeforeSave(final T entity) {
        final List<MetaProperty<?>> props = entity.getProperties().values().stream().
                filter(p -> p.shouldAssignBeforeSave()).collect(Collectors.toList());
        if (!props.isEmpty()) {
            final DateTime rightNow = now.get();
            if (rightNow == null) {
                throw new EntityCompanionException("The now() constant has not been assigned!");
            }

            for (final MetaProperty<?> prop : props) {
                final Object value = prop.getValue();
                if (value == null) {
                    if (User.class.isAssignableFrom(prop.getType())) {
                        final User usr = user.get();
                        if (usr == null) {
                            throw new EntityCompanionException("The user could not be determined!");
                        }
                        prop.setValue(usr);
                    } else if (Date.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(rightNow.toDate());
                    } else if (DateTime.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(rightNow);
                    } else {
                        assignBeforeSave.accept(prop);
                    }

                    if (prop.getValue() == null) {
                        throw new EntityCompanionException(format("Property %s@%s is marked as assignable before save, but no value could be determined.", prop.getName(), entity.getType().getName()));
                    }
                }
            }
        }
    }


}
