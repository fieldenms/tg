package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.handlers.IAfterSave;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.TransactionDate;
import ua.com.fielden.platform.entity.annotation.TransactionUser;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.IFilter;
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
import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.inject.Inject;
import com.google.inject.Injector;

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

    /**
     * A principle constructor.
     *
     * @param entityType
     */
    @Inject
    protected CommonEntityDao(final IFilter filter) {
        this.filter = filter;
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

    @Override
    @SessionRequired
    public T lazyLoad(final Long id) {
        final List<T> result = new EntityFetcher(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, null, null, universalConstants).getLazyEntitiesOnPage(from(select(getEntityType()).where().prop(AbstractEntity.ID).eq().val(id).model()).model(), 0, 1);

        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * Saves the provided entity. This method checks entity version and throws StaleObjectStateException if the provided entity is stale. There is no in-memory referential
     * integrity guarantee -- the returned instance is always a different instance. However, from the perspective of data loading, it is guaranteed that the object graph of the
     * returned instance contains the object graph of the passed in entity as its subgraph.
     */
    @Override
    @SessionRequired
    public T save(final T entity) {
        if (entity == null) {
            throw new IllegalArgumentException(format("Null reference to entity of type %s cannot be saved.", getEntityType()));
        } else if (!entity.isDirty()) {
            logger.debug(format("Entity %s is not dirty (ID = %s). Saving is skipped. Entity refetched.", entity, entity.getId()));
            return findById(entity.getId(), FetchModelReconstructor.reconstruct(entity));
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
        // let's make sure that entity is not a duplicate
        final AggregatedResultQueryModel model = select(createQueryByKey(entity.getKey())).yield().prop(AbstractEntity.ID).as(AbstractEntity.ID).modelAsAggregate();
        final List<EntityAggregates> ids = new EntityFetcher(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, null, null, universalConstants).getEntities(from(model).model());
        final int count = ids.size();
        if (count == 1 && !(entity.getId().longValue() == ((Number) ids.get(0).get(AbstractEntity.ID)).longValue())) {
            throw Result.failure(
                    entity,
                    new IllegalArgumentException(format("Entity \"%s\" of type %s already exists.", entity, TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey())));
        }

        // load the entity directly from the session
        final T persistedEntity = (T) getSession().load(entity.getType(), entity.getId());
        // check for data staleness and try to resolve the conflict is possible (refer #83)
        if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion() && !canResolveConflict(entity, persistedEntity)) {
            throw Result.failure(entity, new IllegalStateException(format("Could not resolve conflicting changes. Entity %s (%s) could not be saved.", entity.getKey(), TitlesDescsGetter.getEntityTitleAndDesc(getEntityType()).getKey())));
        }

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final fetch<T> entityFetch = FetchModelReconstructor.reconstruct(entity);
        // proceed with property assignment from entity to persistent entity, which in case of a resolvable conflict acts like a fetch/rebase in git
        final boolean isActivatable = entity instanceof ActivatableAbstractEntity;

        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            final String propName = prop.getName();
            final Object value = prop.getValue();

            // it is essential that if a property is of an entity type it should be re-associated with the current session before being set
            // the easiest way to do that is to load entity by id using the current session

            if (false) { // prop.isActivatable()
                // if value is null then an activatable entity has been dereferenced and its refCount needs to be decremented
                if (value == null) {
                    // get the latest value of the dereferenced activatable as the current value of the persisted entity version from the database and decrement its ref count
                    // previous property value should not be null as it would become dirty, also, there was no property conflict, so it can be safely assumed that previous value is NOT null
                    final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), persistedEntity.<AbstractEntity<?>> get(propName).getId());
                    getSession().update(persistedValue.decRefCount());

                    // assign null as the property value to actually dereference activatable
                    persistedEntity.set(propName, null);
                } else { // otherwise there could be either referencing or a reference change
                    if (persistedEntity.<AbstractEntity<?>> get(propName) != null) { // need to decrement refCount for the dereferenced entity
                        final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), persistedEntity.<AbstractEntity<?>> get(propName).getId());
                        getSession().update(persistedValue.decRefCount());
                    }
                    // also need increment refCount for a newly referenced activatable
                    final ActivatableAbstractEntity<?> persistedValue = (ActivatableAbstractEntity<?>) getSession().load(prop.getType(), ((AbstractEntity<?>) value).getId());
                    getSession().update(persistedValue.incRefCount());

                    // assign updated activatable as the property value
                    persistedEntity.set(propName, persistedValue);
                }
            } else if (value instanceof AbstractEntity && !(value instanceof PropertyDescriptor) && !(value instanceof AbstractUnionEntity)) {
                persistedEntity.set(propName, getSession().load(((AbstractEntity<?>) value).getType(), ((AbstractEntity<?>) value).getId()));
            } else {
                persistedEntity.set(propName, value);
            }
        }
        // check if entity is valid after changes
        final Result res = persistedEntity.isValid();
        if (res.isSuccessful()) {
            getSession().update(persistedEntity);
            getSession().flush();
            getSession().clear();
        } else {
            throw res;
        }

        return findById(persistedEntity.getId(), entityFetch);
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
            if ((persistedEntity.get(name) == null && entity.get(name) != null) ||
                    (persistedEntity.get(name) != null && entity.get(name) == null) ||
                    !persistedEntity.get(name).equals(entity.get(name))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Persists an entity that was not persisted before.
     *
     * @param entity
     */
    private T saveNewEntity(final T entity) {
        // let's make sure that entity is not a duplicate
        final Integer count = count(createQueryByKey(entity.getKey()), Collections.<String, Object> emptyMap());
        if (count > 0) {
            throw Result.failure(
                    entity,
                    new IllegalArgumentException(format("Entity \"%s\" of type %s already exists.", entity, TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey())));
        }

        // reconstruct entity fetch model for future retrieval at the end of the method call
        final fetch<T> entityFetch = FetchModelReconstructor.reconstruct(entity);
        // check and assign properties annotated with @TransactionDate
        try {
            assignTransactionDate(entity);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not assign transaction date properties.", e);
        }
        // check and assign properties annotated with @TransactionUser
        try {
            assignTransactionUser(entity);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not assign transaction user properties.", e);
        }

        // new entity might be activatable, but this has no effect on its refCount -- should be zero as no other entity could yet reference it
        // however, it might reference other activatable entities, which warrants update to their refCount.
        final boolean shouldProcessActivatableProperties;
        if (entity instanceof ActivatableAbstractEntity) {
            final ActivatableAbstractEntity<?> activatable = (ActivatableAbstractEntity<?>) entity;
            shouldProcessActivatableProperties = activatable.isActive();
        } else {
            shouldProcessActivatableProperties = true;
        }

        if (shouldProcessActivatableProperties) {
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> activatableDirtyProperties = collectActivatableDirtyProperties(entity);

            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : activatableDirtyProperties) {
                if (prop.getValue() != null) {
                    // need to update refCount for the activatable entity
                    final ActivatableAbstractEntity<?> value = prop.getValue();
                    final IEntityDao co = getCoFinder().find(value.getType());

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
            getSession().flush();
            getSession().clear();
        } else {
            throw result;
        }

        return (T) entity.resetMetaState(); //findById(entity.getId(), entityFetch);
    }

    /**
     * Collects properties that represent dirty activatable entities.
     *
     * @param entity
     * @return
     */
    private Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> collectActivatableDirtyProperties(final T entity) {
        final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> dirtyActivatable = new HashSet<>();
        for (final MetaProperty<?> prop : entity.getProperties().values()) {
            if (prop.isDirty() && ActivatableAbstractEntity.class.isAssignableFrom(prop.getType())) {
                // null values correspond to dereferencing and should be allowed only for already persisted entities
                if (prop.getValue() != null || entity.isPersisted()) {
                    dirtyActivatable.add((MetaProperty<? extends ActivatableAbstractEntity<?>>) prop);
                }
            }
        }
        return dirtyActivatable;
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

    private void assignTransactionDate(final T entity) throws Exception {
        final List<Field> transactionDateProperties = Finder.findRealProperties(entity.getType(), TransactionDate.class);
        if (!transactionDateProperties.isEmpty()) {
            final DateTime now = universalConstants.now();
            if (now == null) {
                throw new IllegalArgumentException("The now() constant has not been assigned!");
            }
            for (final Field property : transactionDateProperties) {
                property.setAccessible(true);
                final Object value = property.get(entity);
                if (value == null) {
                    if (Date.class.isAssignableFrom(property.getType())) {
                        property.set(entity, now.toDate());
                    } else if (DateTime.class.isAssignableFrom(property.getType())) {
                        property.set(entity, now);
                    } else {
                        throw new IllegalArgumentException("The type of property " + entity.getType().getName() + "@" + property.getName()
                                + " is not valid for annotation TransactionDate.");
                    }
                }
            }
        }
    }

    private void assignTransactionUser(final T entity) throws Exception {
        final List<Field> transactionUserProperties = Finder.findRealProperties(entity.getType(), TransactionUser.class);
        if (!transactionUserProperties.isEmpty()) {
            final User user = getUser();
            if (user == null) {
                throw new IllegalArgumentException("The user could not be determined!");
            }
            for (final Field property : transactionUserProperties) {
                property.setAccessible(true);
                final Object value = property.get(entity);
                if (value == null) {
                    if (User.class.isAssignableFrom(property.getType())) {
                        property.set(entity, user);
                    } else if (String.class.isAssignableFrom(property.getType())) {
                        property.set(entity, user.getKey());
                    } else {
                        throw new IllegalArgumentException("The type of property " + entity.getType().getName() + "@" + property.getName()
                                + " is not valid for annotation TransactionUser.");
                    }
                }
            }
        }
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
        return evalNumOfPages(model, paramValues, 1);
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
        return new EntityFetcher(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants).getEntitiesOnPage(queryModel, pageNumber, pageCapacity);
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
        final int numberOfPages = pageCount > 0 ? pageCount : evalNumOfPages(model.getQueryModel(), model.getParamValues(), pageCapacity);
        final int pageNumber = pageNo < 0 ? numberOfPages - 1 : pageNo;
        return new EntityQueryPage(model, pageNumber, pageCapacity, numberOfPages);
    }

    @Override
    @SessionRequired
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        final List<T> data = getFirstEntities(model, 2);
        if (data.size() > 1) {
            throw new IllegalArgumentException("The provided query model leads to retrieval of more than one entity (" + data.size() + ").");
        }
        return data.size() == 1 ? data.get(0) : null;
    }

    @Override
    @SessionRequired
    public IPage<T> getPage(final int pageNo, final int pageCapacity) {
        final int numberOfPages = evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity);
        final int pageNumber = pageNo < 0 ? numberOfPages - 1 : pageNo;
        return new EntityQueryPage(getDefaultQueryExecutionModel(), pageNumber, pageCapacity, numberOfPages);
    }

    @Override
    public Session getSession() {
        if (session == null) {
            throw new RuntimeException("Someone forgot to annotate some method with SessionRequired!");
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
    protected int evalNumOfPages(final QueryModel<T> model, final Map<String, Object> paramValues, final int pageCapacity) {
        final AggregatedResultQueryModel countQuery = model instanceof EntityResultQueryModel ? select((EntityResultQueryModel<T>) model).yield().countAll().as("count").modelAsAggregate()
                : select((AggregatedResultQueryModel) model).yield().countAll().as("count").modelAsAggregate();
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> countModel = from(countQuery).with(paramValues).with(fetch(EntityAggregates.class).with("count")).lightweight(true).model();
        final List<EntityAggregates> counts = new EntityFetcher(getSession(), getEntityFactory(), getCoFinder(), domainMetadata, filter, getUsername(), universalConstants). //
        getEntities(countModel);
        final int resultSize = ((Number) counts.get(0).get("count")).intValue();

        return resultSize % pageCapacity == 0 ? resultSize / pageCapacity : resultSize / pageCapacity + 1;
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
            throw new Result(new IllegalArgumentException("Null is not an acceptable value for an entity instance."));
        }
        if (!entity.isPersisted()) {
            throw new Result(new IllegalArgumentException("Only persisted entity instances can be deleted."));
        }
        try {
            getSession().createQuery("delete " + getEntityType().getName() + " where id = " + entity.getId()).executeUpdate();
        } catch (final ConstraintViolationException e) {
            throw new Result(new IllegalStateException("This entity could not be deleted due to existing dependencies."));
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
            throw new Result(new IllegalArgumentException("Null is not an acceptable value for eQuery model."));
        }

        final List<T> toBeDeleted = getAllEntities(from(model).with(paramValues).lightweight(true).model());

        for (final T entity : toBeDeleted) {
            defaultDelete(entity);
        }
    }

    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model) {
        defaultDelete(model, Collections.<String, Object> emptyMap());
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
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
        private final int numberOfPages;
        private final int pageCapacity;
        private final List<T> data;
        private final QueryExecutionModel<T, ?> queryModel;
        private final T summary;

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final int pageNumber, final int pageCapacity, final int numberOfPages) {
            this(queryModel, (QueryExecutionModel<T, ?>) null, pageNumber, pageCapacity, numberOfPages);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final QueryExecutionModel<T, ?> summaryModel, final int pageNumber, final int pageCapacity, final int numberOfPages) {
            this(queryModel, summaryModel != null ? calcSummary(summaryModel) : null, pageNumber, pageCapacity, numberOfPages);
        }

        public EntityQueryPage(final QueryExecutionModel<T, ?> queryModel, final T summary, final int pageNumber, final int pageCapacity, final int numberOfPages) {
            this.pageNumber = pageNumber;
            this.pageCapacity = pageCapacity;
            this.numberOfPages = numberOfPages == 0 ? 1 : numberOfPages;
            this.queryModel = queryModel;
            data = getEntitiesOnPage(queryModel, pageNumber, pageCapacity);

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
                    return new EntityQueryPage(queryModel, summary, pageNumber + 1, pageCapacity, numberOfPages);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, pageNumber + 1, pageCapacity, numberOfPages);
                } else {
                    throw new IllegalStateException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public IPage<T> prev() {
            if (hasPrev()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, pageNumber - 1, pageCapacity, numberOfPages);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, pageNumber - 1, pageCapacity, numberOfPages);
                } else {
                    throw new IllegalStateException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public IPage<T> first() {
            if (hasPrev()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, 0, pageCapacity, numberOfPages);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, 0, pageCapacity, numberOfPages);
                } else {
                    throw new IllegalStateException("There was no query provided to retrieve the data.");
                }
            }
            return null;
        }

        @Override
        public IPage<T> last() {
            if (hasNext()) {
                if (queryModel != null && summary != null) {
                    return new EntityQueryPage(queryModel, summary, numberOfPages - 1, pageCapacity, numberOfPages);
                } else if (queryModel != null) {
                    return new EntityQueryPage(queryModel, numberOfPages - 1, pageCapacity, numberOfPages);
                } else {
                    throw new IllegalStateException("There was no query provided to retrieve the data.");
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