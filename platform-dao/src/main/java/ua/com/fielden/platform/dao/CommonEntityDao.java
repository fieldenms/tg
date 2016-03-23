package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.Injector;

import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.dao.handlers.IAfterSave;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityBatchDeleterByIds;
import ua.com.fielden.platform.entity.query.EntityBatchDeleterByQueryModel;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.Validators;

/**
 * This is a most common Hibernate-based implementation of the {@link IEntityDao}.
 * <p>
 * It should not be used directly -- more preferred way is to inherit it for implementation of a more specific DAO.
 * <p>
 * Property <code>session</code> is used to allocation session whenever is appropriate -- all data access methods should use this session. It is envisaged that the real class usage
 * will include Guice method intercepter that would assign session instance dynamically before executing calls to methods annotated with {@link SessionRequired}.
 *
 * @author TG Team
 *
 * @param <T>
 *            -- entity type
 * @param <K>
 *            -- entitie's key type
 */
public abstract class CommonEntityDao<T extends AbstractEntity<?>> extends AbstractEntityDao<T> implements ISessionEnabled {

    private final Logger logger = Logger.getLogger(this.getClass());

    private Session session;

    private DomainMetadata domainMetadata;
    
    private IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;

    private EntityFactory entityFactory;

    @Inject
    private ICompanionObjectFinder coFinder;

    @Inject
    private Injector injector;

    private final IFilter filter;

    @Inject
    private IUniversalConstants universalConstants;
    @Inject
    private IUserProvider up;

    /** A marker to skip re-fetching an entity during save. */
    private boolean skipRefetching = false;
    
    /** A guard against an accidental use of quick save to prevent its use for companions with overridden method <code>save</code>.
     *  Refer issue <a href='https://github.com/fieldenms/tg/issues/421'>#421</a> for more details. */
    private final boolean hasSaveOverridden;

    /**
     * A principle constructor.
     *
     * @param entityType
     */
    protected CommonEntityDao(final IFilter filter) {
        this.filter = filter;
        this.hasSaveOverridden = isSaveOverridden();
    }
    
    private boolean isSaveOverridden() {
        // let's check if method save was overridden
        try {
            final Method methodSave = getClass().getMethod("save", getEntityType());
            if (methodSave != null && methodSave.getDeclaringClass() != CommonEntityDao.class) {
                return true;
            }
        } catch (NoSuchMethodException | SecurityException e) {
        }
        
        return false;
    }

    /**
     * A setter for injection of entityFactory instance.
     *
     * @param entityFactory
     */
    @Inject
    protected void setEntityFactory(final EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /**
     * A separate setter is used in order to avoid enforcement of providing mapping generator as one of constructor parameter in descendant classes.
     *
     * @param mappingExtractor
     */
    @Inject
    protected void setDomainMetadata(final DomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }


    @Inject
    protected void setIdOnlyProxiedEntityTypeCache(final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
    }

    /**
     * Cancels currently running query.
     *
     * @return
     */
    @Override
    public boolean stop() {
        final Session sess = getSession();
        try {
            sess.cancelQuery();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /**
     * By default all DAO computations are considered indefinite. Thus returning <code>null</code> as the result.
     */
    @Override
    public Integer progress() {
        return null;
    }

    @Override
    public final String getUsername() {
        final User user = getUser();
        return user != null ? user.getKey() : null;
    }

    @Override
    @SessionRequired
    public T findById(final Long id, final fetch<T> fetchModel) {
        return super.findById(id, fetchModel);
    }

    @Override
    @SessionRequired
    public T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues) {
        return super.findByKeyAndFetch(fetchModel, keyValues);
    }

    @Override
    @SessionRequired
    public T findByKey(final Object... keyValues) {
        return super.findByKey(keyValues);
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    @SessionRequired
    public final long quickSave(final T entity) {
        if (hasSaveOverridden) {
            throw new EntityCompanionException(
                    format("Quick save is not supported for entity [%s] due to an overridden method save (refer companion [%s]).", 
                            getEntityType().getName(), 
                            getEntityType().getAnnotation(CompanionObject.class).value().getName()));
        }
        
        
        try {
            skipRefetching = true;
            return save(entity).getId();
        } finally {
            skipRefetching = false;
        }
    }
    
    /**
     * Saves the provided entity. This method checks entity version and throws StaleObjectStateException if the provided entity is stale. There is no in-memory referential
     * integrity guarantee -- the returned instance is always a different instance. However, from the perspective of data loading, it is guaranteed that the object graph of the
     * returned instance contains the object graph of the passed in entity as its subgraph (i.e. it can be wider, but not narrower).
     */
    @Override
    @SessionRequired
    public T save(final T entity) {
        if (entity == null) {
            throw new EntityCompanionException(format("Null entity of type [%s] cannot be saved.", getEntityType().getName()));
        } else if (!entity.isPersistent()) {
            return entity;
        } else if (!entity.isInstrumented()) {
            throw new EntityCompanionException(format("Uninstrumented entity of type [%s] cannot be saved.", getEntityType().getName()));
        } else if (!entity.isDirty() && entity.isValid().isSuccessful()) {
            logger.debug(format("Entity [%s] is not dirty (ID = %s). Saving is skipped. Entity refetched.", entity, entity.getId()));
            return skipRefetching ? entity : findById(entity.getId(), FetchModelReconstructor.reconstruct(entity));
        }
        logger.debug(format("Start saving entity %s (ID = %s)", entity, entity.getId()));

        // need to capture names of dirty properties before the actual saving takes place and makes all properties not dirty
        // this is needed for executing after save event handler
        final List<String> dirtyProperties = toStringList(entity.getDirtyProperties());

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
        processAfterSaveEvent(resultantEntity, dirtyProperties);

        return resultantEntity;
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
        final AggregatedResultQueryModel model = select(createQueryByKey(entity.getKey())).yield().prop(AbstractEntity.ID).as(AbstractEntity.ID).modelAsAggregate();
        final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, null, null, universalConstants, idOnlyProxiedEntityTypeCache);
        final List<EntityAggregates> ids = new EntityFetcher(queryExecutionContext).getEntities(from(model).model());
        final int count = ids.size();
        if (count == 1 && !(entity.getId().longValue() == ((Number) ids.get(0).get(AbstractEntity.ID)).longValue())) {
            throw new EntityCompanionException(format("Entity \"%s\" of type %s already exists.", entity, TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey()));
        }

        // load the entity directly from the session
        final T persistedEntity = (T) getSession().load(entity.getType(), entity.getId());
        persistedEntity.setIgnoreEditableState(true);
        // check for data staleness and try to resolve the conflict is possible (refer #83)
        if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion() && !canResolveConflict(entity, persistedEntity)) {
            throw new EntityCompanionException(format("Could not resolve conflicting changes. Entity %s (%s) could not be saved.", entity.getKey(), TitlesDescsGetter.getEntityTitleAndDesc(getEntityType()).getKey()));
        }

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching ? Optional.empty() : Optional.of(FetchModelReconstructor.reconstruct(entity));

        // proceed with property assignment from entity to persistent entity, which in case of a resolvable conflict acts like a fetch/rebase in git
        // it is essential that if a property is of an entity type it should be re-associated with the current session before being set
        // the easiest way to do that is to load entity by id using the current session
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            final Object value = prop.getValue();
            if (shouldProcessAsActivatable(entity, prop)) {
                handleDirtyActivatableProperty(entity, persistedEntity, prop, value);
            } else if (value instanceof AbstractEntity && !(value instanceof PropertyDescriptor) && !(value instanceof AbstractUnionEntity)) {
                persistedEntity.set(prop.getName(), getSession().load(((AbstractEntity<?>) value).getType(), ((AbstractEntity<?>) value).getId()));
            } else {
                persistedEntity.set(prop.getName(), value);
            }
        } // end of processing dirty properties

        // handle ref counts of non-dirty activatable properties
        if (entity instanceof ActivatableAbstractEntity) {
            handleNonDirtyActivatableIfNecessary(entity, persistedEntity);
        }

        // check if entity is valid after all the changes above
        final Result res = persistedEntity.isValid();
        if (res.isSuccessful()) {
            getSession().update(persistedEntity);
            persistedEntity.resetMetaState();
            getSession().flush();
            getSession().clear();
        } else {
            throw res;
        }

        return entityFetchOption.isPresent() ? findById(persistedEntity.getId(), entityFetchOption.get()) : persistedEntity;
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
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), persistedEntity.<AbstractEntity<?>> get(propName).getId());
            // if persistedValue active and does not equal to the entity being saving then need to decrement its refCount
            if (!beingActivated && persistedValue.isActive() && !entity.equals(persistedValue)) { // avoid counting self-references
                persistedValue.setIgnoreEditableState(true);
                getSession().update(persistedValue.decRefCount());
            }

            // assign null as the property value to actually dereference activatable
            persistedEntity.set(propName, null);
        } else { // otherwise there could be either referencing (i.e. before property was null) or a reference change (i.e. from one value to some other)
            // need to process previous property value
            final AbstractEntity<?> prevValue = persistedEntity.<AbstractEntity<?>> get(propName);
            if (prevValue != null && !entity.equals(prevValue)) { // need to decrement refCount for the dereferenced entity, but avoid counting self-references
                final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), persistedEntity.<AbstractEntity<?>> get(propName).getId());
                persistedValue.setIgnoreEditableState(true);
                getSession().update(persistedValue.decRefCount());
            }
            // also need increment refCount for a newly referenced activatable
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), ((AbstractEntity<?>) value).getId());
            if (!entity.equals(persistedValue)) { // avoid counting self-references
                // now let's check if the entity itself is an active activatable
                // as this influences the decision to increment refCount for the newly referenced activatable
                // because, if it's not then there is no reason to increment refCout for the referenced instance
                // in other words, inactive entity does not count as an active referencer
                if (entity.<Boolean> get(ACTIVE) == true) {
                    persistedValue.setIgnoreEditableState(true);
                    getSession().update(persistedValue.incRefCount());
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
            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : collectActivatableNotDirtyProperties(entity, keyMembers)) {
                // get value from a persisted version of entity, whch is loaded by Hibernate
                // if a corresponding property is proxied due to insufficient fetch model, its value is retrieved lazily by Hibernate
                final AbstractEntity<?> value = persistedEntity.get(prop.getName());
                if (value != null) { // if there is actually some value
                    // load activatable value
                    final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), value.getId());
                    persistedValue.setIgnoreEditableState(true);
                    // if activatable property value is not a self-reference
                    // then need to check if it is active and if so increment its refCount
                    // otherwise, if activatable is not active then we've got an erroneous situation that should prevent activation of entity
                    if (!entity.equals(persistedValue)) {
                        if (activeProp.getValue()) { // is entity being activated?
                            if (!persistedValue.isActive()) { // if activatable is not active then this is an error
                                throw new EntityCompanionException(format("Entity %s has a reference to already inactive entity %s (type %s)", entity, persistedValue, prop.getType()));
                            } else { // otherwise, increment refCount
                                getSession().update(persistedValue.incRefCount());
                            }
                        } else if (persistedValue.isActive()) { // is entity being deactivated, but is referencing an active activatable?
                            getSession().update(persistedValue.decRefCount());
                        }
                    }
                }
            }

            // separately need to perform de-activation of deactivatable dependencies in case where the entity being saved is deactivated
            if (!activeProp.getValue()) {
                final List<? extends ActivatableAbstractEntity<?>> deactivatables = Validators.findActiveDeactivatableDependencies((ActivatableAbstractEntity<?>) entity, getCoFinder());
                for (final ActivatableAbstractEntity<?> deactivatable : deactivatables) {
                    deactivatable.set(ACTIVE, false);
                    final Result result = deactivatable.isValid();
                    if (result.isSuccessful()) {
                        // persisting of deactivatables should go through the logic of companion save
                        // and cannot be persisted by just using a call to Hibernate Session
                        final CommonEntityDao co = getCoFinder().find(deactivatable.getType());
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
        if (prop.isActivatable() && entity instanceof ActivatableAbstractEntity) {
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
        final Integer count = count(createQueryByKey(entity.getKey()), Collections.<String, Object> emptyMap());
        if (count > 0) {
            throw new EntityCompanionException(format("Entity \"%s\" of type %s already exists.", entity, TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey()));
        }

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching ? Optional.empty() : Optional.of(FetchModelReconstructor.reconstruct(entity));
        // process transactional assignments
        assignPropertiesBeforeSave(entity);

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
                    final CommonEntityDao co = getCoFinder().find(value.getType());

                    // get the latest value from the database, reassign it and update its ref count
                    final fetch fetch = FetchModelReconstructor.reconstruct(value);
                    final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) co.findById(value.getId(), fetch);
                    entity.beginInitialising();
                    entity.set(prop.getName(), persistedValue);
                    entity.endInitialising();
                    final Result assignmentResult = prop.revalidate(false);

                    if (!assignmentResult.isSuccessful()) {
                        throw assignmentResult;
                    }
                    co.save(persistedValue.incRefCount());
                }
            }
        }

        // save the entity
        final Result result = entity.isValid();
        if (result.isSuccessful()) {
            getSession().save(entity);
            entity.resetMetaState();
            getSession().flush();
            getSession().clear();
        } else {
            throw result;
        }

        return entityFetchOption.isPresent() ? findById(entity.getId(), entityFetchOption.get()) : entity;
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
            if (prop.isDirty() && prop.isActivatable()) {
                addToResultIfApplicableFromActivatablePerspective(entity, keyMembers, result, prop);
            }
        }
        return result;
    }

    /**
     * Collects properties that represent not dirty activatable properties
     *
     * @param entity
     * @return
     */
    private Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> collectActivatableNotDirtyProperties(final T entity, final Set<String> keyMembers) {
        final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result = new HashSet<>();
        for (final MetaProperty<?> prop : entity.getProperties().values()) {
            // proxied property is considered to be not dirty in this context
            final boolean notDirty = prop.isProxy() || !prop.isDirty(); 
            if (notDirty && prop.isActivatable()) {
                addToResultIfApplicableFromActivatablePerspective(entity, keyMembers, result, prop);
            }
        }
        return result;
    }

    /**
     * A helper method to determine which of the provided properties should be handled upon save from the perspective of activatable entity logic (update of refCount).
     * <p>
     * A remark: the proxied activatable properties need to be handled from the perspective of activatable entity logic (update of refCount).
     *
     * @param entity
     * @param keyMembers
     * @param result
     * @param prop
     */
    private void addToResultIfApplicableFromActivatablePerspective(final T entity, final Set<String> keyMembers, final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result, final MetaProperty<?> prop) {
        // let's first identify whether entity belongs to the deactivatable type of the referenced property type
        // if so, it should not inflict any ref counts for this property
        final Class<? extends ActivatableAbstractEntity<?>> type = (Class<? extends ActivatableAbstractEntity<?>>) prop.getType();
        final DeactivatableDependencies ddAnnotation = type.getAnnotation(DeactivatableDependencies.class);
        boolean belongsToDeactivatableDependencies;
        if (ddAnnotation != null) {
            // if the main type belongs to dependent deactivatables of the type for the current property,
            // and that property is a key member then such property should be excluded from standard processing of dirty activatables
            belongsToDeactivatableDependencies = keyMembers.contains(prop.getName()) && Arrays.asList(ddAnnotation.value()).contains(entity.getType());
        } else {
            belongsToDeactivatableDependencies = false;
        }
        // null values correspond to dereferencing and should be allowed only for already persisted entities
        // checking prop.isProxy() is really just to prevent calling prop.getValue() on proxied properties, which fails with StrictProxyException
        // this also assumes that proxied properties might actually have a value and need to be included for further processing
        // values for proxied properties are then retrieved in a lazy fashion by Hibernate
        if (!belongsToDeactivatableDependencies && (prop.isProxy() || prop.getValue() != null || entity.isPersisted())) {
            result.add((MetaProperty<? extends ActivatableAbstractEntity<?>>) prop);
        }
    }

    private List<String> toStringList(final List<MetaProperty<?>> dirtyProperties) {
        final List<String> result = new ArrayList<>(dirtyProperties.size());
        for (final MetaProperty<?> prop : dirtyProperties) {
            result.add(prop.getName());
        }
        return result;
    }

    private void processAfterSaveEvent(final T entity, final List<String> dirtyProperties) {
        try {
            final AfterSave afterSave = AnnotationReflector.getAnnotation(getClass(), AfterSave.class);
            // if after save annotation is present then need to instantiate the declared event handler.
            if (afterSave != null) {
                final Class<? extends IAfterSave<T>> typeForAfterSaveHandler = (Class<? extends IAfterSave<T>>) afterSave.value();
                final IAfterSave<T> handler = injector.getInstance(typeForAfterSaveHandler);
                handler.perfrom(entity, dirtyProperties);
            }
        } catch (final Exception ex) {
            logger.warn("Could not process after save event.", ex);
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
            final DateTime now = universalConstants.now();
            if (now == null) {
                throw new EntityCompanionException("The now() constant has not been assigned!");
            }

            for (final MetaProperty<?> prop : props) {
                final Object value = prop.getValue();
                if (value == null) {
                    if (User.class.isAssignableFrom(prop.getType())) {
                        final User user = getUser();
                        if (user == null) {
                            throw new EntityCompanionException("The user could not be determined!");
                        }
                        prop.setValue(user);
                    } else if (Date.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(now.toDate());
                    } else if (DateTime.class.isAssignableFrom(prop.getType())) {
                        prop.setValue(now);
                    } else {
                        assignBeforeSave(prop);
                    }

                    if (prop.getValue() == null) {
                        throw new EntityCompanionException(format("Property %s@%s is marked as assignable before save, but no value could be determined.", prop.getName(), entity.getType().getName()));
                    }
                }
            }
        }
    }

    /**
     * This method is used during saving of the modified entity, which has been persisted previously, and ensures that no removal of required assignable before save properties has
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
     * A method for assigning a value to a domain specific transactional property. This method does nothing by default, and should be overridden by companion objects in order to
     * provide domain specific behaviour.
     *
     * @param prop
     */
    protected void assignBeforeSave(final MetaProperty<?> prop) {

    }

    @Override
    @SessionRequired
    public boolean isStale(final Long entityId, final Long version) {
        if (entityId == null) {
            return false;
        }

        final Integer count = ((Number) getSession().createQuery("select count(*) from " + getEntityType().getName() + " where id = :id and version = :version")//
        .setParameter("id", entityId).setParameter("version", version).uniqueResult()).intValue();

        return count != 1;
    }

    @Override
    @SessionRequired
    public boolean entityExists(final T entity) {
        return entityExists(entity.getId());
    }

    @Override
    @SessionRequired
    public boolean entityExists(final Long id) {
        if (id == null) {
            return false;
        }
        return getSession().createQuery("select id from " + getEntityType().getName() + " where id = :in_id").setLong("in_id", id).uniqueResult() != null;
    }

    @Override
    @SessionRequired
    public int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        return evalNumOfPages(model, paramValues, 1).getKey();
    }

    @Override
    @SessionRequired
    public int count(final EntityResultQueryModel<T> model) {
        return count(model, Collections.<String, Object> emptyMap());
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of {@link QueryExecutionModel}.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    protected List<T> getEntitiesOnPage(final QueryExecutionModel<T, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants, idOnlyProxiedEntityTypeCache);
        return new EntityFetcher(queryExecutionContext).getEntitiesOnPage(queryModel, pageNumber, pageCapacity);
    }

    @Override
    @SessionRequired
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> query) {
        return getEntitiesOnPage(query, null, null);
    }

    @Override
    @SessionRequired
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities) {
        return getEntitiesOnPage(query, 0, numberOfEntities);
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by query with no filtering conditions. Useful for things like autocompleters.
     */
    @Override
    @SessionRequired
    public IPage<T> firstPage(final int pageCapacity) {
        return new EntityQueryPage(getDefaultQueryExecutionModel(), 0, pageCapacity, evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity));
    }

    /**
     * Returns a first page holding up to <code>size</code> instance of entities retrieved by the provided query model. This allows a query based pagination.
     */
    @Override
    @SessionRequired
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final int pageCapacity) {
        return new EntityQueryPage(model, 0, pageCapacity, evalNumOfPages(model.getQueryModel(), model.getParamValues(), pageCapacity));
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by the provided query model with appropriate summary model. This allows a query
     * based pagination.
     */
    @Override
    @SessionRequired
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        return new EntityQueryPage(model, summaryModel, 0, pageCapacity, evalNumOfPages(model.getQueryModel(), model.getParamValues(), pageCapacity));
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCapacity) {
        return getPage(model, pageNo, 0, pageCapacity);
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        final Pair<Integer, Integer> numberOfPagesAndCount = pageCount > 0 ? Pair.pair(pageCount, pageCount * pageCapacity) : evalNumOfPages(model.getQueryModel(), model.getParamValues(), pageCapacity);

        final int pageNumber = pageNo < 0 ? numberOfPagesAndCount.getKey() - 1 : pageNo;
        return new EntityQueryPage(model, pageNumber, pageCapacity, numberOfPagesAndCount);
    }

    @Override
    @SessionRequired
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        final List<T> data = getFirstEntities(model, 2);
        if (data.size() > 1) {
            throw new UnexpectedNumberOfReturnedEntities(format("The provided query model leads to retrieval of more than one entity (%s).", data.size()));
        }
        return data.size() == 1 ? data.get(0) : null;
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final int pageNo, final int pageCapacity) {
        final Pair<Integer, Integer> numberOfPagesAndCount = evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity);
        final int pageNumber = pageNo < 0 ? numberOfPagesAndCount.getKey() - 1 : pageNo;
        return new EntityQueryPage(getDefaultQueryExecutionModel(), pageNumber, pageCapacity, numberOfPagesAndCount);
    }

    @Override
    public Session getSession() {
        if (session == null) {
            throw new EntityCompanionException("Session is missing, most likely, due to missing @SessionRequired annotation.");
        }
        return session;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Calculates the number of pages of the given size required to fit the whole result set.
     *
     *
     * @param model
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    protected Pair<Integer, Integer> evalNumOfPages(final QueryModel<T> model, final Map<String, Object> paramValues, final int pageCapacity) {
        final AggregatedResultQueryModel countQuery = model instanceof EntityResultQueryModel ? select((EntityResultQueryModel<T>) model).yield().countAll().as("count").modelAsAggregate()
                : select((AggregatedResultQueryModel) model).yield().countAll().as("count").modelAsAggregate();
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> countModel = from(countQuery).with(paramValues).with(fetchAggregates().with("count")).lightweight().model();

        final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants, idOnlyProxiedEntityTypeCache);
        final List<EntityAggregates> counts = new EntityFetcher(queryExecutionContext).getEntities(countModel);

        final int resultSize = ((Number) counts.get(0).get("count")).intValue();

        final Integer pageSize = resultSize % pageCapacity == 0 ? resultSize / pageCapacity : resultSize / pageCapacity + 1;

        return Pair.pair(pageSize, resultSize);
    }

    /**
     * Should return a byte array representation the exported data in a format envisaged by the specific implementation.
     * <p>
     * For example it could be a byte array of GZipped Excel data.
     *
     * @param query
     *            -- query result of which should be exported.
     * @param propertyNames
     *            -- names of properties, including dot notated properties, which should be used in the export.
     * @param propertyTitles
     *            -- titles corresponding to the properties being exported, which are used as headers of columns.
     * @return
     * @throws IOException
     */
    @Override
    @SessionRequired
    public byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        // run the query and iterate through result exporting the data
        final List<T> result = getEntitiesOnPage(query, null, null);

        return WorkbookExporter.convertToByteArray(WorkbookExporter.export(result, propertyNames, propertyTitles));
    }

    /**
     * A convenient default implementation for entity deletion, which should be used by overriding method {@link #delete(Long)}.
     *
     * @param entity
     */
    @SessionRequired
    protected void defaultDelete(final T entity) {
        if (entity == null) {
            throw new EntityCompanionException("Null is not an acceptable value for an entity instance.");
        }
        if (!entity.isPersisted()) {
            throw new EntityCompanionException("Only persisted entity instances can be deleted.");
        }
        try {
            getSession().createQuery("delete " + getEntityType().getName() + " where id = " + entity.getId()).executeUpdate();
        } catch (final ConstraintViolationException e) {
            throw new EntityCompanionException("This entity could not be deleted due to existing dependencies.");
        }
    }

    /**
     * A convenient default implementation for deletion of entities specified by provided query model.
     *
     * @param entity
     */
    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }

        final List<T> toBeDeleted = getAllEntities(from(model).with(paramValues).lightweight().model());

        for (final T entity : toBeDeleted) {
            defaultDelete(entity);
        }
    }

    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model) {
        defaultDelete(model, Collections.<String, Object> emptyMap());
    }

    /**
     * A convenient default implementation for batch deletion of entities specified by provided query model.
     *
     * @param entity
     */
    @SessionRequired
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        if (model == null) {
            throw new EntityCompanionException("Null is not an acceptable value for eQuery model.");
        }
        
        final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants, idOnlyProxiedEntityTypeCache);

        return new EntityBatchDeleterByQueryModel(queryExecutionContext).deleteEntities(model, paramValues);
    }

    @SessionRequired
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model) {
        return defaultBatchDelete(model, Collections.<String, Object> emptyMap());
    }

    @SessionRequired
    protected int defaultBatchDelete(final List<? extends AbstractEntity<?>> entities) {
        return defaultBatchDeleteByPropertyValues(ID, entities);
    }
    
    @SessionRequired
    protected int defaultBatchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDeleteByPropertyValues(ID, entitiesIds);
    }
    
    @SessionRequired
    protected int defaultBatchDeleteByPropertyValues(final String propName, final List<? extends AbstractEntity<?>> entities) {
        Set<Long> ids = new HashSet<>();
        for (AbstractEntity<?> entity : entities) {
            ids.add(entity.getId());
        }
        return batchDelete(ids);
    }
    
    @SessionRequired
    protected int defaultBatchDeleteByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        if (entitiesIds.size() == 0) {
            throw new EntityCompanionException("No entities ids have been provided for deletion.");
        }

        return new EntityBatchDeleterByIds(getSession(), domainMetadata.getPersistedEntityMetadataMap().get(getEntityType())).deleteEntities(propName, entitiesIds);
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }

    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return idOnlyProxiedEntityTypeCache;
    }

    @Override
    public User getUser() {
        return up.getUser();
    }

    public IFilter getFilter() {
        return filter;
    }

    /**
     * Calculates summary based on the assumption that <code>model</code> represents summary model for the type T.
     */
    private T calcSummary(final QueryExecutionModel<T, ?> model) {
        final List<T> list = getAllEntities(model);
        return list.size() == 1 ? list.get(0) : null;
    }

    /**
     * Implements pagination based on the provided query.
     *
     * @author TG Team
     *
     */
    public class EntityQueryPage implements IPage<T> {
        private final int pageNumber; // zero-based
        private final Pair<Integer, Integer> numberOfPagesAndCount;
        private final int numberOfPages;
        private final int pageCapacity;
        private final List<T> data;
        private final QueryExecutionModel<T, ?> queryModel;
        private final T summary;

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this(queryModel, (QueryExecutionModel<T, ?>) null, pageNumber, pageCapacity, numberOfPagesAndCount);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final QueryExecutionModel<T, ?> summaryModel, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this(queryModel, summaryModel != null && numberOfPagesAndCount.getValue() > 0 ? calcSummary(summaryModel) : null, pageNumber, pageCapacity, numberOfPagesAndCount);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final T summary, final int pageNumber, final int pageCapacity, final Pair<Integer, Integer> numberOfPagesAndCount) {
            this.numberOfPagesAndCount = numberOfPagesAndCount;
            this.pageNumber = pageNumber;
            this.pageCapacity = pageCapacity;
            this.numberOfPages = numberOfPagesAndCount.getKey() == 0 ? 1 : numberOfPagesAndCount.getKey();
            this.queryModel = queryModel;
            this.data = numberOfPagesAndCount.getValue() > 0 ? getEntitiesOnPage(queryModel, pageNumber, pageCapacity) : new ArrayList<>();

            this.summary = summary;
        }

        @Override
        public T summary() {
            return summary;
        }

        @Override
        public int capacity() {
            return pageCapacity;
        }

        @Override
        public List<T> data() {
            return hasNext() ? data.subList(0, capacity()) : data;
        }

        @Override
        public boolean hasNext() {
            return pageNumber < numberOfPages - 1;
        }

        @Override
        public boolean hasPrev() {
            return no() > 0;
        }

        @Override
        public IPage<T> next() {
            if (hasNext()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, pageNumber + 1, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, pageNumber + 1, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public IPage<T> prev() {
            if (hasPrev()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, pageNumber - 1, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, pageNumber - 1, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public IPage<T> first() {
            if (hasPrev()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, 0, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, 0, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public IPage<T> last() {
            if (hasNext()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, numberOfPages - 1, pageCapacity, numberOfPagesAndCount);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, numberOfPages - 1, pageCapacity, numberOfPagesAndCount);
                } else {
                    throw new EntityCompanionException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public int numberOfPages() {
            return numberOfPages;
        }

        @Override
        public String toString() {
            return "Page " + (no() + 1) + " of " + numberOfPages;
        }

        @Override
        public int no() {
            return pageNumber;
        }
    }

    public ICompanionObjectFinder getCoFinder() {
        return coFinder;
    }

    public IUniversalConstants getUniversalConstants() {
        return universalConstants;
    }

}
