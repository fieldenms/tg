package ua.com.fielden.platform.dao;

import com.google.inject.Injector;
import com.google.inject.Provider;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;
import ua.com.fielden.platform.companion.*;
import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.continuation.NeedMoreDataStorage;
import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.handlers.IAfterSave;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.*;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.ioc.session.SessionInterceptor;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.Reflector.isMethodOverriddenOrDeclared;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.utils.Lazy.lazyProvider;
import static ua.com.fielden.platform.utils.Lazy.lazySupplier;

/// Base class for database-aware implementations of entity companions.
/// It is suitable for companions for both persistent and action entities.
///
/// Method injection is used to spare subclasses from declaring large constructors that merely delegate to `super`.
/// This approach also improves evolvability, allowing changes in a backward-compatible manner.
///
public abstract class CommonEntityDao<T extends AbstractEntity<?>> extends AbstractEntityReader<T> implements IEntityDao<T>, ISessionEnabled, ICanReadUninstrumented {

    private final Logger logger = getLogger();

    // *** INJECTABLE FIELDS
    private IDbVersionProvider dbVersionProvider;
    private ICompanionObjectFinder coFinder;
    private Injector injector;
    private IUniversalConstants universalConstants;
    private IUserProvider userProvider;
    private EntityFactory entityFactory;
    private IEntityFetcher entityFetcher;
    private Supplier<DeleteOperations<T>> deleteOps;
    private EntityBatchInsertOperation.Factory batchInsertOpsFactory;
    private Supplier<PersistentEntitySaver<T>> entitySaver;
    // ***

    /** Session-scoped. Set by {@link SessionInterceptor} */
    private Session session;

    /** Session-scoped. Set by {@link SessionInterceptor} */
    private String transactionGuid;

    /** A guard against an accidental use of quick save to prevent its use for companions with overridden method <code>save</code>.
     *  Refer issue <a href='https://github.com/fieldenms/tg/issues/421'>#421</a> for more details. */
    private Boolean hasSaveOverridden;

    private boolean $instrumented$ = true;

    private final Class<? extends Comparable<?>> keyType;
    private final Class<T> entityType;
    private IFetchProvider<T> fetchProvider;

    /**
     * The default constructor, which looks for annotation {@link EntityType} to identify the entity type automatically.
     * An exception is thrown if the annotation is missing.
     * <p>
     * <b>Deprecated</b>: use the no-arg constructor (the {@code super} call is no longer necessary).
     */
    @Deprecated(forRemoval = true)
    protected CommonEntityDao(final IFilter filter) {
        this();
    }

    protected CommonEntityDao() {
        final EntityType annotation = AnnotationReflector.getAnnotation(getClass(), EntityType.class);
        if (annotation == null) {
            throw new EntityCompanionException(format("Companion object [%s] is missing @EntityType annotation.", getClass().getName()));
        }
        this.entityType = (Class<T>) annotation.value();
        this.keyType = AnnotationReflector.getKeyType(entityType);
    }

    @Inject
    protected void setDeleteOpsFactory(final Provider<DeleteOperations.Factory> deleteOpsFactory) {
        deleteOps = lazyProvider(() -> deleteOpsFactory.get().create(this, this::getSession, entityType));
    }

    @Inject
    protected void setBatchInsertOpsFactory(final EntityBatchInsertOperation.Factory batchInsertOpsFactory) {
        this.batchInsertOpsFactory = batchInsertOpsFactory;
    }

    @Inject
    protected void setPersistentEntitySaverFactory(final Provider<PersistentEntitySaver.IFactory> factory) {
        entitySaver = lazySupplier(() -> factory.get().create(
                this::getSession,
                this::getTransactionGuid,
                entityType,
                keyType,
                this::processAfterSaveEvent,
                this::assignBeforeSave,
                this::findById,
                this::exists,
                logger));
    }

    @Inject
    protected void setDbVersionProvider(final IDbVersionProvider dbVersionProvider) {
        this.dbVersionProvider = dbVersionProvider;
    }

    @Inject
    protected void setCoFinder(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Inject
    protected void setInjector(final Injector injector) {
        this.injector = injector;
    }

    @Inject
    protected void setUniversalConstants(final IUniversalConstants universalConstants) {
        this.universalConstants = universalConstants;
    }

    @Inject
    protected void setUserProvider(final IUserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Inject
    protected void setEntityFactory(final EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Inject
    protected void setEntityFetcher(final IEntityFetcher entityFetcher) {
        this.entityFetcher = entityFetcher;
    }

    @Override
    public DbVersion getDbVersion() {
        return dbVersionProvider.dbVersion();
    }

    @Override
    protected IEntityFetcher entityFetcher() {
        return entityFetcher;
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
     * By default, all DAO computations are considered indefinite. Thus returning empty result.
     */
    @Override
    public Optional<Integer> progress() {
        return Optional.empty();
    }

    @Override
    public final String getUsername() {
        final User user = getUser();
        return user != null ? user.getKey() : null;
    }

    @Override
    @SessionRequired
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> query) {
        return getEntitiesOnPage(query, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SessionRequired
    public long quickSave(final T entity) {
        if (hasSaveOverridden == null) {
            hasSaveOverridden = isMethodOverriddenOrDeclared(CommonEntityDao.class, getClass(), "save", getEntityType());
        }
        if (hasSaveOverridden) {
            throw new EntityCompanionException(
                    format("Quick save is not supported for entity [%s] due to an overridden method save (refer companion [%s]).",
                            getEntityType().getName(),
                            getEntityType().getAnnotation(CompanionObject.class).value().getName()));
        }

        if (entity == null) {
            throw new EntityCompanionException(format("Null entity of type [%s] cannot be saved.", entityType.getName()));
        } else if (!entity.isPersistent()) {
            throw new EntityCompanionException(format("Quick save is not supported for non-persistent entity [%s].", entityType.getName()));
        } else {
            final Long id = entitySaver.get().coreSave(entity, true, empty())._1;
            if (id == null) {
                throw new EntityCompanionException(format("Saving of entity [%s] did not return its ID.", entityType.getName()));
            }
            return id;
        }
    }

    @Override
    @SessionRequired
    public T save(final T entity) {
        if (entity == null) {
            throw new EntityCompanionException(format("Null entity of type [%s] cannot be saved.", entityType.getName()));
        }
        if (this instanceof ISaveWithFetch<?> it) {
            return ((ISaveWithFetch<T>) it).save(entity, Optional.of(FetchModelReconstructor.reconstruct(entity))).asRight().value();
        }
        if (!entity.isPersistent()) {
            return entity;
        } else {
            return entitySaver.get().save(entity);
        }
    }

    /**
     * Experimental API with the potential to replace {@link #save(AbstractEntity)} if proven superior in practice.
     * <p>
     * This method could be used as an alternative to {@link #quickSave(AbstractEntity)} by passing in an empty instance of {@code Optional<fetch<T>>}.
     * The right way to go about it, would be to override this method and place all the logic into this method instead of the potentially overridden {@link #save(AbstractEntity)},
     * and simply call it from {@link #save(AbstractEntity)} with the appropriate fetch model.
     * This way would guarantee a single path for validation and other related logic when saving entities.
     * <p>
     * The return type {@code Either<Long, T>} represents either an entity id (left) or an entity instance (right).
     * Passing an empty instance of {@code Optional<fetch<T>>} should always skip refetching and return the left result (i.e. id) – this is analogous to {@link #quickSave(AbstractEntity)}.
     *
     * @param entity
     * @param maybeFetch
     * @return
     */
    @SessionRequired
    protected Either<Long, T> save(final T entity, final Optional<fetch<T>> maybeFetch) {
        // if maybeFetch is empty then we skip re-fetching
        final boolean skipRefetching = !maybeFetch.isPresent();
        final T2<Long, T> result = entitySaver.get().coreSave(entity, skipRefetching, maybeFetch);
        return skipRefetching ? left(result._1) : right(result._2);
    }

    /// Returns an open session, if present.
    /// Otherwise, throws [EntityCompanionException] exception.
    ///
    @Override
    public Session getSession() {
        if (session == null) {
            throw new EntityCompanionException("Session is missing, most likely, due to missing @SessionRequired annotation.");
        } else if (!session.isOpen()) {
            throw new EntityCompanionException("Session is closed, most likely, due to missing @SessionRequired annotation.");
        }
        return session;
    }

    /// Returns a session instances without any checks.
    /// It is intended mainly for testing purposes to ensure correctness of the session state after various db operations.
    ///
    public @Nullable Session getSessionUnsafe() {
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

    @Override
    public User getUser() {
        return userProvider.getUser();
    }

    protected ICompanionObjectFinder getCoFinder() {
        return coFinder;
    }

    public IUniversalConstants getUniversalConstants() {
        return universalConstants;
    }

    /**
     * Just a convenience method for obtaining the current date/time as a single call {@code now()} instead of chaining {@code getUniversalConstants().now()}.
     * @return
     */
    public DateTime now() {
        return getUniversalConstants().now();
    }

    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> co$Cache = new HashMap<>();
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> coCache = new HashMap<>();

    /**
     * A convenient way to obtain companion instances by the types of corresponding entities.
     *
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co$(final Class<E> type) {
        if (instrumented() && getEntityType().equals(type)) {
            return (C) this;
        }

        IEntityDao<?> co = co$Cache.get(type);
        if (co == null) {
            co = getCoFinder().find(type, false);
            co$Cache.put(type, co);
        }
        return (C) co;
    }

    /**
     * A convenient way to obtain a companion as a reader that reads uninstrumented entities.
     *
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends IEntityReader<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
        if (!instrumented() && getEntityType().equals(type)) {
            return (C) this;
        }

        IEntityDao<?> co = coCache.get(type);
        if (co == null) {
            co = getCoFinder().find(type, true);
            coCache.put(type, co);
        }
        return (C) co;
    }

    @Override
    public void readUninstrumented() {
        this.$instrumented$ = false;
    }

    /**
     * This method is inherited from {@link AbstractEntityReader} and overridden to inform the reader when should it read uninstrumented entities.
     *
     * @return
     */
    @Override
    public boolean instrumented() {
        return $instrumented$;
    }

    //-------------------------------------------------------------------//
    //--------------------- Continuation related methods ----------------//
    //-------------------------------------------------------------------//

    /// A convenient method to set a single "more data" instance for a given key.
    /// Mostly useful for unit tests.
    ///
    public CommonEntityDao<T> setMoreData(final String key, final IContinuationData moreData) {
        NeedMoreDataStorage.putMoreData(key, moreData);
        return this;
    }

    /// A convenient way to obtain “more data” by key.
    /// Returns an empty optional if no corresponding data is found.
    ///
    /// @param key a companion object property that identifies the continuation
    /// @return an optional containing the requested data if found; otherwise, empty
    ///
    public <E extends IContinuationData> Optional<E> moreData(final String key) {
        return NeedMoreDataStorage.moreData(key);
    }

    /// A convenient way to obtain all "more data" by keys.
    ///
    public Map<String, IContinuationData> moreData() {
        return NeedMoreDataStorage.moreData();
    }

    /// A convenient helper for creating unique values for [NeedMoreData] keys.
    /// Use this method when defining keys for continuation data in a consistent,
    /// type-safe manner suitable for storage in the continuation context.
    ///
    /// This method is intended for initialising static constants that represent
    /// continuation-data keys.
    ///
    /// For example:
    /// ```java
    /// public static final String
    ///     MDK_CERTIFICATION = mkMoreDataKey(CertificationValidation.class);
    /// ```
    ///
    /// @param moreDataType the type of the continuation data.
    /// @return a newly generated continuation-data key
    ///
    public static String moreDataKey(final Class<? extends IContinuationData> moreDataType) {
        try {
            final var fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            final var simpleName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            return simpleName + "-" + moreDataType.getSimpleName();
        } catch (final Exception ex) {
            // A fallback just in case.
            return UUID.randomUUID().toString();
        }
    }

    //-------------------------------------------------------------------//
    //------------------ Before and After save methods ------------------//
    //-------------------------------------------------------------------//

    /// A method for assigning a value to a domain specific transactional property.
    /// This method does nothing by default, and should be overridden by companion objects in order to
    /// provide domain specific behaviour.
    ///
    protected void assignBeforeSave(final MetaProperty<?> prop) {

    }

    private void processAfterSaveEvent(final T entity, final Set<String> dirtyProperties) {
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

    //-------------------------------------------------------------------//
    //---------------- Block of default delete methods ------------------//
    //-------------------------------------------------------------------//

    /// A convenient default implementation for entity deletion, which should be used when overriding method [#delete(T)].
    ///
    @SessionRequired
    protected void defaultDelete(final T entity) {
        deleteOps.get().defaultDelete(entity);
    }

    /// A convenient default implementation for deletion of entities specified by provided query model and parameters, which could be empty.
    ///
    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        deleteOps.get().defaultDelete(model, paramValues);
    }

    /// The same as [#defaultDelete(EntityResultQueryModel,Map)], but with empty parameters.
    ///
    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model) {
        deleteOps.get().defaultDelete(model);
    }

    /// A convenient default implementation for batch deletion of entities specified by provided query model and parameters, which could be empty.
    ///
    @SessionRequired
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        return deleteOps.get().defaultBatchDelete(model, paramValues);
    }

    /// The same as [#defaultBatchDelete(EntityResultQueryModel,Map)], but with empty parameters.
    ///
    @SessionRequired
    protected int defaultBatchDelete(final EntityResultQueryModel<T> model) {
        return deleteOps.get().defaultBatchDelete(model);
    }

    /// Batch deletion of entities in the provided list.
    ///
    @SessionRequired
    protected int defaultBatchDelete(final List<? extends AbstractEntity<?>> entities) {
        return batchDelete(entities.stream().map(AbstractEntity::getId).toList());
    }

    /// Batch deletion of entities by their ID values.
    ///
    @SessionRequired
    protected int defaultBatchDelete(final Collection<Long> entitiesIds) {
        return deleteOps.get().defaultBatchDelete(entitiesIds);
    }

    /// A more generic version of batch deletion of entities [#defaultBatchDelete(Collection)] that accepts a property name and a collection of ID values.
    /// Those entities that have the specified property matching any of those ID values get deleted.
    ///
    @SessionRequired
    protected int defaultBatchDeleteByPropertyValues(final String propName, final Collection<Long> entitiesIds) {
        return deleteOps.get().defaultBatchDeleteByPropertyValues(propName, entitiesIds);
    }

    /// The same as [#defaultBatchDeleteByPropertyValues(String,Collection)], but for a list of entities.
    ///
    @SessionRequired
    protected <E extends AbstractEntity<?>> int defaultBatchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        return deleteOps.get().defaultBatchDeleteByPropertyValues(propName, propEntities);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return keyType;
    }

    @Override
    public final IFetchProvider<T> getFetchProvider() {
        if (fetchProvider == null) {
            fetchProvider = createFetchProvider();
        }
        return fetchProvider;
    }

    /// Creates a fetch provider for this entity companion.
    ///
    /// Override this method to provide a custom fetch provider.
    ///
    protected IFetchProvider<T> createFetchProvider() {
        // Provides a very minimalistic version of fetch provider by default (only id and version are included).
        return EntityUtils.fetch(getEntityType());
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /// Instantiates an instrumented new entity of the type for which this object is a companion.
    /// The default entity constructor, which should be protected, is used for instantiation.
    ///
    @Override
    public T new_() {
        return entityFactory.newEntity(getEntityType());
    }

    /// Provides the default implementation for batch insertion of new entities.
    ///
    /// This method serves as the standard implementation for batch insertion operations in entity companions.
    /// It handles the common logic required for efficiently inserting multiple new entities into the database
    /// while ensuring data integrity and applying necessary pre-save operations.
    ///
    /// The method performs the following operations for each entity:
    /// - Auto-populates required properties for new entities (creation timestamps, user info, etc.).
    /// - Validates each entity before insertion.
    /// - Processes entities in batches according to the specified batch size.
    ///
    /// If validation fails for any entity, a runtime exception is thrown and the entire batch operation is rolled back.
    ///
    /// **Important:**
    /// - Batch insertion is not suitable in many cases and should be used with care.
    ///   Attempts to batch insert *active* activatable entities throws [InvalidArgumentException].
    /// - This method should be called in the scope of an open session.
    ///   Otherwise, [EntityCompanionException] is thrown.
    ///
    /// @param newEntities a stream of new entities to be inserted
    /// @param batchSize   the number of entities to process in each database batch operation;
    ///                    larger values improve throughput but increase memory usage
    ///
    /// @return the total count of successfully inserted entities
    ///
    /// @see EntityBatchInsertOperation#batchInsert for the underlying batch insertion mechanism
    ///
    protected int defaultBatchInsert(final Stream<T> newEntities, final int batchSize) {
        // Call getSession() to make sure defaultBatchInsert is called in the scope of an open session.
        // If not, this call throws exception.
        getSession();

        // If there is an open session, batch insertion cat be attempted.
        final var ops = batchInsertOpsFactory.create(() -> new TransactionalExecution(userProvider, this::getSession));
        final var saver = entitySaver.get();
        return ops.batchInsert(newEntities.filter(Objects::nonNull).peek(entity -> {
            // Do not permit batch insertion for active activatbles.
            if (entity instanceof ActivatableAbstractEntity<?> ae && ae.isActive()) {
                throw new InvalidArgumentException("Batch insertion of active activatable entities [%s] is not supported.".formatted(entity.getType().getSimpleName()));
            }

            // Autopopulated entity props.
            if (entity instanceof AbstractPersistentEntity<?> persistentEntity) {
                saver.assignCreationInfoForNew(persistentEntity);
            }
            saver.assignPropsBeforeSaveForNew(entity);

            // Validate and throw if invalid.
            entity.isValid().ifFailure(Result::throwRuntime);
        }), batchSize);
    }

}
