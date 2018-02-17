package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.reflection.Reflector.isMethodOverriddenOrDeclared;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.Injector;

import ua.com.fielden.platform.companion.AbstractEntityReader;
import ua.com.fielden.platform.companion.DeleteOperations;
import ua.com.fielden.platform.companion.ICanReadUninstrumented;
import ua.com.fielden.platform.companion.PersistentEntitySaver;
import ua.com.fielden.platform.dao.annotations.AfterSave;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.handlers.IAfterSave;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityBatchDeleteByIdsOperation;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IUniversalConstants;

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
public abstract class CommonEntityDao<T extends AbstractEntity<?>> extends AbstractEntityReader<T> implements IEntityDao<T>, ISessionEnabled, ICanReadUninstrumented {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final PersistentEntitySaver<T> entitySaver;
    
    private Session session;
    private String transactionGuid;
    private DomainMetadata domainMetadata;
    private IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    
    @Inject
    private ICompanionObjectFinder coFinder;
    @Inject
    private Injector injector;

    private final IFilter filter;
    private final DeleteOperations<T> deleteOps;

    @Inject
    private IUniversalConstants universalConstants;
    @Inject
    private IUserProvider up;

    /** A marker to skip re-fetching an entity during save. */
    private boolean skipRefetching = false;
    
    /** A guard against an accidental use of quick save to prevent its use for companions with overridden method <code>save</code>.
     *  Refer issue <a href='https://github.com/fieldenms/tg/issues/421'>#421</a> for more details. */
    private Boolean hasSaveOverridden;

    private boolean $instrumented$ = true;

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
        this.deleteOps = new DeleteOperations<>(
                this,
                this::getSession,
                entityType,
                this::newQueryExecutionContext,
                () -> new EntityBatchDeleteByIdsOperation<T>(getSession(), (PersistedEntityMetadata<T>) getDomainMetadata().getPersistedEntityMetadataMap().get(entityType)));
        
        entitySaver = new PersistentEntitySaver<>(
                this::getSession,
                this::getTransactionGuid,
                entityType,
                keyType,
                this::getUser,
                () -> getUniversalConstants().now(),
                () -> skipRefetching,
                this::isFilterable,
                this::getCoFinder,
                this::newQueryExecutionContext,
                this::processAfterSaveEvent,
                this::assignBeforeSave,
                this::findById,
                this::count,
                logger);
                
    }

    /**
     * A helper method to create new instances of {@link QueryExecutionContext}.
     * @return
     */
    @Override
    protected QueryExecutionContext newQueryExecutionContext() {
        return new QueryExecutionContext(
                getSession(), 
                getEntityFactory(), 
                getCoFinder(), 
                getDomainMetadata(), 
                getFilter(), 
                getUsername(), 
                getUniversalConstants(), 
                getIdOnlyProxiedEntityTypeCache());
    }

    @Override
    protected boolean isFilterable() {
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
    @SessionRequired
    @Deprecated
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> query) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? query.lightweight() : query;
        try (final Stream<T> stream = stream(qem)) {
            return stream.collect(toList());
        }
    }

    @Override
    @SessionRequired
    @Deprecated
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities) {
        final QueryExecutionModel<T, ?> qem = !instrumented() ? query.lightweight() : query;
        try (final Stream<T> stream = stream(qem, numberOfEntities)) {
            return stream.limit(numberOfEntities).collect(toList());
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    @SessionRequired
    public final long quickSave(final T entity) {
        if (hasSaveOverridden == null) {
            hasSaveOverridden = isMethodOverriddenOrDeclared(CommonEntityDao.class, getClass(), "save", getEntityType());
        }
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
    
    @Override
    @SessionRequired
    public T save(final T entity) {
        if (entity == null) {
            throw new EntityCompanionException(format("Null entity of type [%s] cannot be saved.", entityType.getName()));
        } else if (!entity.isPersistent()) {
            return entity;
        } else {
            return entitySaver.save(entity);
        }
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

    protected ICompanionObjectFinder getCoFinder() {
        return coFinder;
    }

    public IUniversalConstants getUniversalConstants() {
        return universalConstants;
    }
    
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> co$Cache = new HashMap<>();
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> coCache = new HashMap<>();    
    
    /**
     * A convenient way to obtain companion instances by the types of corresponding entities.
     * 
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co$(final Class<E> type) {
        if (getEntityType().equals(type)) {
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
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
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

    ////////////////////////////////////////////////////////////////
    //////////////////// Before and After save methods /////////////
    /**
     * A method for assigning a value to a domain specific transactional property. This method does nothing by default, and should be overridden by companion objects in order to
     * provide domain specific behaviour.
     *
     * @param prop
     */
    protected void assignBeforeSave(final MetaProperty<?> prop) {

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

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return keyType;
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
