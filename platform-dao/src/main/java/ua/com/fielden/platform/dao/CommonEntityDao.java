package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static org.hibernate.LockOptions.UPGRADE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.Injector;

import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.dao.handlers.IAfterSave;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
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
public abstract class CommonEntityDao<T extends AbstractEntity<?>> implements IEntityDao<T>, ISessionEnabled {

    private static final String ERR_MSG_NO_QUERY_PROVIDED = "There was no query provided to retrieve the data.";
    
    private final Logger logger = Logger.getLogger(this.getClass());

    private Session session;
    private String transactionGuid;

    private DomainMetadata domainMetadata;
    
    private IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;

    @Inject
    private ICompanionObjectFinder coFinder;

    @Inject
    private Injector injector;

    private final IFilter filter;
    
    private final CommonEntityCompanionDeleteOperations<T> deleteOps;

    @Inject
    private IUniversalConstants universalConstants;
    @Inject
    private IUserProvider up;

    /** A marker to skip re-fetching an entity during save. */
    private boolean skipRefetching = false;
    
    /** A guard against an accidental use of quick save to prevent its use for companions with overridden method <code>save</code>.
     *  Refer issue <a href='https://github.com/fieldenms/tg/issues/421'>#421</a> for more details. */
    private final boolean hasSaveOverridden;

    private boolean instrumented = true;

    private final Class<? extends Comparable<?>> keyType;
    private final Class<T> entityType;
    private IFetchProvider<T> fetchProvider;
    
    @Inject
    private EntityFactory entityFactory;

    /**
     * The default constructor, which looks for annotation {@link EntityType} to identify the entity type automatically.
     * An exception is thrown if the annotation is missing. 
     *
     * @param entityType
     */
    protected CommonEntityDao(final IFilter filter) {
        final EntityType annotation = AnnotationReflector.getAnnotation(getClass(), EntityType.class);
        if (annotation == null) {
            throw new EntityCompanionException(format("Companion object [%s] is missing @EntityType annotation.", getClass().getName()));
        }
        this.entityType = (Class<T>) annotation.value();
        this.keyType = AnnotationReflector.getKeyType(entityType);
        
        this.filter = filter;
        this.hasSaveOverridden = isSaveOverridden();
        this.deleteOps = new CommonEntityCompanionDeleteOperations<>(this);
    }

    protected boolean getFilterable() {
        return false;
    }
    
    @Override
    public boolean instrumented() {
        return instrumented;
    }

    @Override
    public <E extends IEntityDao<T>> E uninstrumented() {
        if (!instrumented) {
            return (E) this;
        }
        
        final Class<?> coType = PropertyTypeDeterminator.stripIfNeeded(getClass());
        final CommonEntityDao<T> co = (CommonEntityDao<T>) injector.getInstance(coType);
        co.instrumented = false;
        return (E) co;
    }
    
    private boolean isSaveOverridden() {
        // let's check if method save was overridden
        try {
            final Method methodSave = getClass().getMethod("save", getEntityType());
            if (methodSave != null && methodSave.getDeclaringClass() != CommonEntityDao.class) {
                return true;
            }
        } catch (NoSuchMethodException | SecurityException e) {
            logger.debug(e);
        }
        
        return false;
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
            logger.warn(ex);
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
    public T findById(final Long id) {
        return findById(id, null);
    }

    @Override
    @SessionRequired
    public T findById(final Long id, final fetch<T> fetchModel) {
        return fetchOneEntityInstance(id, fetchModel);
    }

    @Override
    @SessionRequired
    public T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues) {
        try {
            return getEntity(instrumented() ? from((createQueryByKey(keyValues))).with(fetchModel).model() : from((createQueryByKey(keyValues))).with(fetchModel).lightweight().model());
        } catch (final EntityCompanionException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    @SessionRequired
    public T findByKey(final Object... keyValues) {
        return findByKeyAndFetch(null, keyValues);
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
        final List<EntityAggregates> ids = new EntityFetcher(queryExecutionContext).getEntities(from(model).lightweight().model());
        final int count = ids.size();
        if (count == 1 && entity.getId().longValue() != ((Number) ids.get(0).get(AbstractEntity.ID)).longValue()) {
            throw new EntityCompanionException(format("%s [%s] already exists.", TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // load the entity directly from the session
        final T persistedEntity = (T) getSession().load(entity.getType(), entity.getId());
        persistedEntity.setIgnoreEditableState(true);
        // check for data staleness and try to resolve the conflict is possible (refer #83)
        if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion() && !canResolveConflict(entity, persistedEntity)) {
            throw new EntityCompanionException(format("Could not resolve conflicting changes. %s [%s] could not be saved.", TitlesDescsGetter.getEntityTitleAndDesc(getEntityType()).getKey(), entity));
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

        // perform meta-data assignment to capture the information about this modification
        if (entity instanceof AbstractPersistentEntity) {
            assignLastModificationInfo((AbstractPersistentEntity<?>) entity, (AbstractPersistentEntity<?>) persistedEntity);
        }

        // update entity
        getSession().update(persistedEntity);
        getSession().flush();
        getSession().clear();

        return entityFetchOption.map(fetch -> findById(persistedEntity.getId(), fetch)).orElse(persistedEntity);
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
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), prevValue.getId(), UPGRADE);
            // if persistedValue active and does not equal to the entity being saving then need to decrement its refCount
            if (!beingActivated && persistedValue.isActive() && !entity.equals(persistedValue)) { // avoid counting self-references
                persistedValue.setIgnoreEditableState(true);
                getSession().update(persistedValue.decRefCount());
            }

            // assign null as the property value to actually dereference activatable
            persistedEntity.set(propName, null);
        } else { // otherwise there could be either referencing (i.e. before property was null) or a reference change (i.e. from one value to some other)
            // need to process previous property value
            final AbstractEntity<?> prevValue = (ActivatableAbstractEntity<?>) entity.getProperty(propName).getPrevValue();
            if (prevValue != null && !entity.equals(prevValue)) { // need to decrement refCount for the dereferenced entity, but avoid counting self-references
                final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), prevValue.getId(), UPGRADE);
                persistedValue.setIgnoreEditableState(true);
                getSession().update(persistedValue.decRefCount());
            }
            // also need increment refCount for a newly referenced activatable
            final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), ((AbstractEntity<?>) value).getId(), UPGRADE);
            if (!entity.equals(persistedValue)) { // avoid counting self-references
                // now let's check if the entity itself is an active activatable
                // as this influences the decision to increment refCount for the newly referenced activatable
                // because, if it's not then there is no reason to increment refCout for the referenced instance
                // in other words, inactive entity does not count as an active referencer
                if (entity.<Boolean>get(ACTIVE)) {
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
            for (final T2<String, Class<ActivatableAbstractEntity<?>>> propNameAndType : collectActivatableNotDirtyProperties(entity, keyMembers)) {
                // get value from a persisted version of entity, which is loaded by Hibernate
                // if a corresponding property is proxied due to insufficient fetch model, its value is retrieved lazily by Hibernate
                final AbstractEntity<?> value = persistedEntity.get(propNameAndType._1);
                if (value != null) { // if there is actually some value
                    // load activatable value
                    final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(propNameAndType._2, value.getId(), UPGRADE);
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
                                getSession().update(persistedValue.incRefCount());
                            }
                        } else if (persistedValue.isActive()) { // is entity being deactivated, but is referencing an active activatable?
                            getSession().update(persistedValue.decRefCount());
                        }
                    }
                }
            }

            // separately need to perform deactivation of deactivatable dependencies in case where the entity being saved is deactivated
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

    private boolean isNotSpecialActivatableToBeSkipped(final MetaProperty<?> prop) {
        return !AbstractPersistentEntity.CREATED_BY.equals(prop.getName()) && !AbstractPersistentEntity.LAST_UPDATED_BY.equals(prop.getName());
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
            throw new EntityCompanionException(format("%s [%s] already exists.", TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey(), entity));
        }

        // process transactional assignments
        if (entity instanceof AbstractPersistentEntity) {
            assignCreationInfo((AbstractPersistentEntity<?>) entity);
        }
        assignPropertiesBeforeSave(entity);
        
        // reconstruct entity fetch model for future retrieval at the end of the method call
        final Optional<fetch<T>> entityFetchOption = skipRefetching ? Optional.empty() : Optional.of(FetchModelReconstructor.reconstruct(entity));

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
                    final ActivatableAbstractEntity<?>  persistedEntity = (ActivatableAbstractEntity<?> ) getSession().load(value.getType(), value.getId(), UPGRADE);
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
                    getSession().update(persistedEntity.incRefCount());
                }
            }
        }

        // save the entity
        getSession().save(entity);
        getSession().flush();
        getSession().clear();

        return entityFetchOption.map(fetch -> findById(entity.getId(), fetch)).orElse(entity);
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

    /**
     * Collects properties that represent not dirty activatable properties
     *
     * @param entity
     * @return
     */
     protected final Set<T2<String, Class<ActivatableAbstractEntity<?>>>> collectActivatableNotDirtyProperties(final T entity, final Set<String> keyMembers) {
        if (entity.isInstrumented()) {
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result = new HashSet<>();
            for (final MetaProperty<?> prop : entity.getProperties().values()) {
                // proxied property is considered to be not dirty in this context
                final boolean notDirty = prop.isProxy() || !prop.isDirty(); 
                if (notDirty && prop.isActivatable() && isNotSpecialActivatableToBeSkipped(prop)) {
                    addToResultIfApplicableFromActivatablePerspective(entity, keyMembers, result, prop);
                }
            }
            return result.stream()
                    .map(prop -> t2(prop.getName(), (Class<ActivatableAbstractEntity<?>>) prop.getType()))
                    .collect(Collectors.toSet());
        } else {
            return Finder.streamRealProperties(entity.getType(), MapTo.class)
                    .filter(field -> ActivatableAbstractEntity.class.isAssignableFrom(field.getType()))
                    .map(field -> t2(field.getName(), (Class<ActivatableAbstractEntity<?>>) field.getType()))
                    .collect(Collectors.toSet());
        }
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

    private void assignCreationInfo(final AbstractPersistentEntity<?> entity) {
        // unit tests utilise a permissive VIRTUAL_USER to persist a "current" user for the testing purposes
        // VIRTUAL_USER is transient and cannot be set as a value for properties of persistent entities
        // thus, a check for VIRTUAL_USER as a current user 
        if (!User.system_users.VIRTUAL_USER.name().equals(getUser().getKey())) {
            entity.set(AbstractPersistentEntity.CREATED_BY, getUser());
            entity.set(AbstractPersistentEntity.CREATED_DATE, universalConstants.now().toDate());
            entity.set(AbstractPersistentEntity.CREATED_TRANSACTION_GUID, getTransactionGuid());
        }
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
        if (!User.system_users.VIRTUAL_USER.name().equals(getUser().getKey())) {
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_BY, getUser());
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_DATE, universalConstants.now().toDate());
            persistentEntity.set(AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID, getTransactionGuid());
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
        final QueryExecutionModel<T, ?> qem = !instrumented() ? queryModel.lightweight() : queryModel;
        
        final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants, idOnlyProxiedEntityTypeCache);
        return new EntityFetcher(queryExecutionContext).getEntitiesOnPage(qem, pageNumber, pageCapacity);
    }
    
    /**
     * Returns a stream of entities that match the provided query.
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    @Override
    @SessionRequired
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel) {
        return stream(queryModel, 100);
    }

    /**
     * Returns a stream of entities that match the provided query. Argument <code>fetchSize</code> provides a hint how many rows should be fetched in a batch at the time of scrolling.
     * 
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    @Override
    @SessionRequired
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel, final int fetchSize) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? queryModel.lightweight() : queryModel;
        
        final QueryExecutionContext queryExecutionContext = new QueryExecutionContext(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants, idOnlyProxiedEntityTypeCache);
        return new EntityFetcher(queryExecutionContext).streamEntities(qem, Optional.of(fetchSize));
    }

    @Override
    @SessionRequired
    @Deprecated
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> query) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? query.lightweight() : query;
        return getEntitiesOnPage(qem, null, null);
    }

    @Override
    @SessionRequired
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? query.lightweight() : query;
        return getEntitiesOnPage(qem, 0, numberOfEntities);
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
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return new EntityQueryPage(qem, 0, pageCapacity, evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity));
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by the provided query model with appropriate summary model. This allows a query
     * based pagination.
     */
    @Override
    @SessionRequired
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return new EntityQueryPage(qem, summaryModel, 0, pageCapacity, evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity));
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        return getPage(qem, pageNo, 0, pageCapacity);
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        
        final Pair<Integer, Integer> numberOfPagesAndCount = pageCount > 0 ? Pair.pair(pageCount, pageCount * pageCapacity) : evalNumOfPages(qem.getQueryModel(), qem.getParamValues(), pageCapacity);

        final int pageNumber = pageNo < 0 ? numberOfPagesAndCount.getKey() - 1 : pageNo;
        return new EntityQueryPage(qem, pageNumber, pageCapacity, numberOfPagesAndCount);
    }

    @Override
    @SessionRequired
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? model.lightweight() : model;
        final List<T> data = getFirstEntities(qem, 2);
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
    
    @Override
    public String getTransactionGuid() {
        if (StringUtils.isEmpty(transactionGuid)) {
            throw new EntityCompanionException("Transaction GUID is missing.");
        }
        return transactionGuid;
    }
    
    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
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
        return WorkbookExporter.convertToGZipByteArray(WorkbookExporter.export(stream(query), propertyNames, propertyTitles));
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
            return Collections.unmodifiableList(data);
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
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
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
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
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
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
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
                    throw new EntityCompanionException(ERR_MSG_NO_QUERY_PROVIDED);
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

    protected ICompanionObjectFinder getCoFinder() {
        return coFinder;
    }

    public IUniversalConstants getUniversalConstants() {
        return universalConstants;
    }
    
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> coCache = new HashMap<>();
    
    /**
     * A convenient way to obtain companion instances by the types of corresponding entities.
     * 
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
        if (getEntityType().equals(type)) {
            return (C) this;
        }
        
        IEntityDao<?> co = coCache.get(type);
        if (co == null) {
            co = getCoFinder().find(type);
            coCache.put(type, co);
        }
        return (C) co;
    }
    
    private final Map<String, IContinuationData> moreData = new HashMap<>();
    
    /**
     * Replaces any previously provided "more data" with new "more data".
     * This is a bulk operation that is mainly needed for the infrastructural integration.
     * 
     * @param moreData
     */
    public CommonEntityDao<T> setMoreData(final Map<String, IContinuationData> moreData) {
        clearMoreData();
        this.moreData.putAll(moreData);
        return this;
    }
    
    /**
     * A convenient method to set a single "more data" instance for a given key. 
     * Mostly useful for unit tests.
     * 
     * @param key
     * @param moreData
     * @return
     */
    public CommonEntityDao<T> setMoreData(final String key, final IContinuationData moreData) {
        this.moreData.put(key, moreData);
        return this;
    }
    
    /**
     * Clears continuations in this companion object.
     */
    public void clearMoreData() {
        this.moreData.clear();
    }
    
    /**
     * A convenient way to obtain "more data" by key. An empty optional is return if there was no "more data" found.
     * 
     * @param key -- companion object property that identifies continuation
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends IContinuationData> Optional<E> moreData(final String key) {
        return Optional.ofNullable((E) this.moreData.get(key));
    }

    /**
     * A convenient way to obtain all "more data" by keys.
     * 
     * @return
     */
    public Map<String, IContinuationData> moreData() {
        return Collections.unmodifiableMap(moreData);
    }
    
    ////////////////////////////////////////////////////////////
    /////////////// block of default delete methods ////////////
    ////////////////////////////////////////////////////////////
    
    /**
     * A convenient default implementation for entity deletion, which should be used by overriding method {@link #delete(Long)}.
     *
     * @param entity
     */
    @SessionRequired
    protected void defaultDelete(final T entity) {
        deleteOps.defaultDelete(entity);
    }

    /**
     * A convenient default implementation for deletion of entities specified by provided query model and parameters, which could be empty.
     * 
     * @param model
     * @param paramValues
     */
    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        deleteOps.defaultDelete(model, paramValues);
    }
    
    /**
     * The same as {@link #defaultDelete(EntityResultQueryModel, Map)}, but with empty parameters.
     * 
     * @param model
     */
    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model) {
        deleteOps.defaultDelete(model);
    }

    /**
     * A convenient default implementation for batch deletion of entities specified by provided query model and parameters, which could be empty.
     * 
     * @param model
     * @param paramValues
     * @return
     */
    @SessionRequired
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        return deleteOps.defaultBatchDelete(model, paramValues);
    }
    
    /**
     * The same as {@link #defaultBatchDelete(EntityResultQueryModel, Map)}, but with empty parameters.
     * 
     * @param model
     * @return
     */
    @SessionRequired
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model) {
        return deleteOps.defaultBatchDelete(model);
    }
    
    /**
     * Batch deletion of entities in the provided list.
     *  
     * @param entities
     * @return
     */
    @SessionRequired
    protected int defaultBatchDelete(final List<? extends AbstractEntity<?>> entities) {
        return batchDelete(entities.stream().map(e -> e.getId()).collect(Collectors.toList()));
    }
    
    /**
     * Batch deletion of entities by their ID values.
     * 
     * @param entitiesIds
     * @return
     */
    @SessionRequired
    protected int defaultBatchDelete(final Collection<Long> entitiesIds) {
        return deleteOps.defaultBatchDelete(entitiesIds);
    }
    
    /**
     * A more generic version of batch deletion of entities {@link #defaultBatchDelete(Collection)} that accepts a property name and a collection of ID values.
     * Those entities that have the specified property matching any of those ID values get deleted. 
     * 
     * @param propName
     * @param entitiesIds
     * @return
     */
    @SessionRequired
    protected int defaultBatchDeleteByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        return deleteOps.defaultBatchDeleteByPropertyValues(propName, entitiesIds);
    }
    
    /**
     * The same as {@link #defaultBatchDeleteByPropertyValues(String, Collection)}, but for a list of entities.
     * 
     * @param propName
     * @param propEntities
     * @return
     */
    @SessionRequired
    protected <E extends AbstractEntity<?>> int defaultBatchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        return deleteOps.defaultBatchDeleteByPropertyValues(propName, propEntities);
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> produceDefaultQueryExecutionModel(final Class<T> entityType) {
        final EntityResultQueryModel<T> query = select(entityType).model();
        query.setFilterable(getFilterable());
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.ID).asc().model();
        return instrumented() ? from(query).with(orderBy).model() : from(query).with(orderBy).lightweight().model();
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> getDefaultQueryExecutionModel() {
        return produceDefaultQueryExecutionModel(entityType);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return keyType;
    }

    private T fetchOneEntityInstance(final Long id, final fetch<T> fetchModel) {
        try {
            final EntityResultQueryModel<T> query = select(getEntityType()).where().prop(AbstractEntity.ID).eq().val(id).model();
            query.setFilterable(getFilterable());
            return getEntity(instrumented() ? from(query).with(fetchModel).model(): from(query).with(fetchModel).lightweight().model());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Method checks whether the key of the entity type associated with this DAO if composite or not.
     *
     * If composite then <code>WHERE</code> statement is build using composite key members and the passed values. The number of values should match the number of composite key
     * members.
     *
     * Otherwise, <code>WHERE</code> statement is build using only property <code>key</code>.
     *
     * The created query expects a unique result, and throws a runtime exception if this is not the case.
     *
     * TODO Need to consider the case of polymorphic associations such as Rotable, which can be both Bogie and/or Wheelset.
     */
    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        final T entity = findByKeyAndFetch(null, keyValues);
        return entity != null;
    }

    @Override
    public T findByEntityAndFetch(final fetch<T> fetchModel, final T entity) {
        if (entity.getId() != null) {
            return findById(entity.getId(), fetchModel);
        } else {
            return findByKeyAndFetch(fetchModel, entity.getKey());
        }
    }

    /**
     * Convenient method for composing a query to select an entity by key value.
     *
     * @param keyValues
     * @return
     */
    protected EntityResultQueryModel<T> createQueryByKey(final Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            throw new IllegalArgumentException("No key values provided.");
        }
        final EntityResultQueryModel<T> query = attachKeyConditions(select(getEntityType()).where(), keyValues).model();
        query.setFilterable(getFilterable());
        return query;
    }
    
    /**
     * Creates a query for entities by their keys (simple or composite). If <code>entitiesWithKeys</code> are empty -- returns empty optional. 
     * 
     * @param entityType -- the entity type
     * @param entitiesWithKeys -- the entities with <b>all</b> key values correctly fetched / assigned
     * @return
     */
    public Optional<EntityResultQueryModel<T>> createQueryByKeyFor(final Collection<T> entitiesWithKeys) {
        IWhere0<T> partQ = select(getEntityType()).where();
        final List<Field> keyMembers = Finder.getKeyMembers(getEntityType());
        
        for (final Iterator<T> iter = entitiesWithKeys.iterator(); iter.hasNext();) {
            final T entityWithKey = iter.next();
            final ICompoundCondition0<T> or = attachKeyConditions(partQ, keyMembers, keyMembers.stream().map(keyMember -> entityWithKey.get(keyMember.getName())).toArray());
            if (iter.hasNext()) {
                partQ = or.or();
            } else {
                return Optional.of(or.model());
            }
        }

        return Optional.empty();
    }
    
    /**
     * Attaches key member conditions to the partially constructed query <code>entryPoint</code> based on its values. 
     * 
     * @param entryPoint
     * @param keyValues
     * @return
     */
    protected ICompoundCondition0<T> attachKeyConditions(final IWhere0<T> entryPoint, final Object... keyValues) {
        return attachKeyConditions(entryPoint, Finder.getKeyMembers(getEntityType()), keyValues);
    }

    /**
     * Attaches key member conditions to the partially constructed query <code>entryPoint</code> based on its values. 
     * 
     * @param entryPoint
     * @param keyMembers
     * @param keyValues
     * @return
     */
    protected ICompoundCondition0<T> attachKeyConditions(final IWhere0<T> entryPoint, final List<Field> keyMembers, final Object... keyValues) {
        if (getKeyType() == DynamicEntityKey.class) {
            // let's be smart about the key values and support the case where an instance of DynamicEntityKey is passed.
            final Object[] realKeyValues;
            if (keyValues.length == 1 && keyValues[0] instanceof DynamicEntityKey) {
                realKeyValues = ((DynamicEntityKey) keyValues[0]).getKeyValues(); 
            } else {
                realKeyValues = keyValues;
            }

            if (keyMembers.size() != realKeyValues.length) {
                throw new EntityCompanionException(format("The number of provided values (%s) does not match the number of properties in the entity composite key (%s).", realKeyValues.length, keyMembers.size()));
            }

            ICompoundCondition0<T> cc = entryPoint.condition(buildConditionForKeyMember(keyMembers.get(0).getName(), keyMembers.get(0).getType(), realKeyValues[0]));

            for (int index = 1; index < keyMembers.size(); index++) {
                cc = cc.and().condition(buildConditionForKeyMember(keyMembers.get(index).getName(), keyMembers.get(index).getType(), realKeyValues[index]));
            }
            return cc;
        } else if (keyValues.length != 1) {
            throw new EntityCompanionException(format("Only one key value is expected instead of %s when looking for an entity by a non-composite key.", keyValues.length));
        } else {
            return entryPoint.condition(buildConditionForKeyMember(AbstractEntity.KEY, getKeyType(), keyValues[0]));
        }
    }

    private ConditionModel buildConditionForKeyMember(final String propName, final Class<?> propType, final Object propValue) {
        if (propValue == null) {
            return cond().prop(propName).isNull().model();
        } else if (String.class.equals(propType)) {
            return cond().lowerCase().prop(propName).eq().lowerCase().val(propValue).model();
        } else if (Class.class.equals(propType)) {
            return cond().prop(propName).eq().val(((Class<?>) propValue).getName()).model();
        } else {
            return cond().prop(propName).eq().val(propValue).model();
        }
    }

    public final IFetchProvider<T> getFetchProvider() {
        if (fetchProvider == null) {
            fetchProvider = createFetchProvider();
        }
        return fetchProvider;
    }

    /**
     * Creates fetch provider for this entity companion.
     * <p>
     * Should be overridden to provide custom fetch provider.
     *
     * @return
     */
    protected IFetchProvider<T> createFetchProvider() {
        // provides a very minimalistic version of fetch provider by default (only id and version are included)
        return EntityUtils.fetch(getEntityType());
    }
    
    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }
 
    /**
     * Instantiates an instrumented new entity of the type for which this object is a companion.
     * The default entity constructor, which should be protected, is used for instantiation.
     *
     * @return
     */
    @Override
    public T new_() {
        return entityFactory.newEntity(getEntityType());
    }

}
