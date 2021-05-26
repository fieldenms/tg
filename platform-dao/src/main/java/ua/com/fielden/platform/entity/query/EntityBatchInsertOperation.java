package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.persistence.FlushModeType;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.ScrollMode;
import org.hibernate.Transaction;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.ExceptionConverter;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.Query;
import org.hibernate.query.spi.NativeQueryImplementor;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.resource.jdbc.spi.JdbcSessionContext;
import org.hibernate.resource.transaction.spi.TransactionCoordinator;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

import com.google.common.collect.Iterators;

import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.eql.meta.Table;

public class EntityBatchInsertOperation {
    private static WrapperOptionsStub wrapperOptionsStub = new WrapperOptionsStub();
    private final DomainMetadata dm;
    private final Supplier<TransactionalExecution> trExecSupplier;
    
    public EntityBatchInsertOperation(final DomainMetadata dm, final Supplier<TransactionalExecution> trExecSupplier) {
        this.dm = dm;
        this.trExecSupplier = trExecSupplier;
    }
    
    public int batchInsert(final List<? extends AbstractEntity<?>> entities, final int batchSize) {
        if (entities.isEmpty()) {
            return 0;
        }
        
        if (entities.stream().anyMatch(ent -> ent.isPersisted())) {
            throw new EntityAlreadyExists("Trying to perform batch insert for persisted entities.");
        }
        
        final Table table = dm.eqlDomainMetadata.getTableForEntityType(entities.get(0).getType());
        final TransactionalExecution trEx = trExecSupplier.get(); // this is a single transaction that will save all batches. 
        
        final List<PropInsertInfo> propInsertInfos = table.columns.entrySet().stream(). //
                filter(entry -> !entry.getKey().equals(ID) && !entry.getKey().equals(VERSION)). //
                map(x -> new PropInsertInfo(x.getKey() + (isPersistedEntityType(x.getValue().type) ? "." + ID : ""), x.getValue().columnName, x.getValue().hibType)). //
                collect(toList());
        
        final List<String> columns = propInsertInfos.stream().map(x -> x.columnName).collect(toList());

        final String insertStmt = generateInsertStmt(table.name, columns);
        final AtomicInteger insertedCount = new AtomicInteger(0);
        Iterators.partition(entities.iterator(), batchSize > 0 ? batchSize : 1) //
                .forEachRemaining(batch -> { //
                    trEx.exec(conn -> {
                        try (final PreparedStatement pst = conn.prepareStatement(insertStmt)) {
                            // batch insert statements
                            batch.forEach(entity -> {
                                try {
                                    int i = 1;
                                    for (final PropInsertInfo item : propInsertInfos) {
                                        final Object value = entity.get(item.leafPropName);
                                        if (item.hibType instanceof UserType) {
                                            ((UserType) item.hibType).nullSafeSet(pst, value, i, wrapperOptionsStub);
                                        } else if (item.hibType instanceof CompositeUserType) {
                                            ((CompositeUserType) item.hibType).nullSafeSet(pst, value, i, wrapperOptionsStub);
                                        } else {
                                            ((Type) item.hibType).nullSafeSet(pst, value, i, wrapperOptionsStub);
                                        }
                                        i = i + 1;
                                    }

                                    pst.addBatch();
                                } catch (final SQLException ex) {
                                    final String error = format("Could not create insert for [%s].", entity);
                                    throw new DbException(error, ex);
                                }
                            });
                            final int[] batchCounts = pst.executeBatch();
                            insertedCount.addAndGet(IntStream.of(batchCounts).sum());
                        } catch (final SQLException ex) {
                            throw new DbException("Could not batch insert generated entities.", ex);
                        }
                    });
                });
        
        return insertedCount.get(); 
    }

    private static String generateInsertStmt(final String tableName, final List<String> columns) {
        return format("INSERT INTO %s(_ID, _VERSION, %s) VALUES(NEXT VALUE FOR %s, 0, %s);", //
                tableName, //
                columns.stream().collect(joining(", ")), // 
                ID_SEQUENCE_NAME, //
                join(", ", nCopies(columns.size(), "?")));
    }

    private static class PropInsertInfo {
        private final String leafPropName;
        private final String columnName;
        private final Object hibType;
        
        private PropInsertInfo(String leafPropName, String columnName, Object hibType) {
            this.leafPropName = leafPropName;
            this.columnName = columnName;
            this.hibType = hibType;
        }
    }
    
    private static class WrapperOptionsStub implements SharedSessionContractImplementor {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean useStreamForLobBinding() {
            return false;
        }

        @Override
        public LobCreator getLobCreator() {
            return null;
        }

        @Override
        public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
            return sqlTypeDescriptor;
        }

        @Override
        public TimeZone getJdbcTimeZone() {
            return null;
        }

        @Override
        public void close() throws HibernateException {
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public Transaction beginTransaction() {
            return null;
        }

        @Override
        public Transaction getTransaction() {
            return null;
        }

        @Override
        public ProcedureCall getNamedProcedureCall(String name) {
            return null;
        }

        @Override
        public ProcedureCall createStoredProcedureCall(String procedureName) {
            return null;
        }

        @Override
        public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
            return null;
        }

        @Override
        public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
            return null;
        }

        @Override
        public Criteria createCriteria(Class persistentClass) {
            return null;
        }

        @Override
        public Criteria createCriteria(Class persistentClass, String alias) {
            return null;
        }

        @Override
        public Criteria createCriteria(String entityName) {
            return null;
        }

        @Override
        public Criteria createCriteria(String entityName, String alias) {
            return null;
        }

        @Override
        public Integer getJdbcBatchSize() {
            return null;
        }

        @Override
        public void setJdbcBatchSize(Integer jdbcBatchSize) {
        }

        @Override
        public Query createNamedQuery(String name) {
            return null;
        }

        @Override
        public JdbcSessionContext getJdbcSessionContext() {
            return null;
        }

        @Override
        public JdbcConnectionAccess getJdbcConnectionAccess() {
            return null;
        }

        @Override
        public TransactionCoordinator getTransactionCoordinator() {
            return null;
        }

        @Override
        public void afterTransactionBegin() {
        }

        @Override
        public void beforeTransactionCompletion() {
        }

        @Override
        public void afterTransactionCompletion(boolean successful, boolean delayed) {
        }

        @Override
        public void flushBeforeTransactionCompletion() {
        }

        @Override
        public boolean shouldAutoJoinTransaction() {
            return false;
        }

        @Override
        public <T> T execute(Callback<T> callback) {
            return null;
        }

        @Override
        public QueryImplementor getNamedQuery(String queryName) {
            return null;
        }

        @Override
        public QueryImplementor createQuery(String queryString) {
            return null;
        }

        @Override
        public <R> QueryImplementor<R> createQuery(String queryString, Class<R> resultClass) {
            return null;
        }

        @Override
        public <R> QueryImplementor<R> createNamedQuery(String name, Class<R> resultClass) {
            return null;
        }

        @Override
        public NativeQueryImplementor createNativeQuery(String sqlString) {
            return null;
        }

        @Override
        public NativeQueryImplementor createNativeQuery(String sqlString, Class resultClass) {
            return null;
        }

        @Override
        public NativeQueryImplementor createNativeQuery(String sqlString, String resultSetMapping) {
            return null;
        }

        @Override
        public NativeQueryImplementor getNamedNativeQuery(String name) {
            return null;
        }

        @Override
        public SessionFactoryImplementor getFactory() {
            return null;
        }

        @Override
        public SessionEventListenerManager getEventListenerManager() {
            return null;
        }

        @Override
        public PersistenceContext getPersistenceContext() {
            return null;
        }

        @Override
        public JdbcCoordinator getJdbcCoordinator() {
            return null;
        }

        @Override
        public JdbcServices getJdbcServices() {
            return null;
        }

        @Override
        public String getTenantIdentifier() {
            return null;
        }

        @Override
        public UUID getSessionIdentifier() {
            return null;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void checkOpen(boolean markForRollbackIfClosed) {
        }

        @Override
        public void markForRollbackOnly() {
        }

        @Override
        public long getTimestamp() {
            return 0;
        }

        @Override
        public boolean isTransactionInProgress() {
            return false;
        }

        @Override
        public Transaction accessTransaction() {
            return null;
        }

        @Override
        public EntityKey generateEntityKey(Serializable id, EntityPersister persister) {
            return null;
        }

        @Override
        public Interceptor getInterceptor() {
            return null;
        }

        @Override
        public void setAutoClear(boolean enabled) {
        }

        @Override
        public void initializeCollection(PersistentCollection collection, boolean writing) throws HibernateException {
        }

        @Override
        public Object internalLoad(String entityName, Serializable id, boolean eager, boolean nullable) throws HibernateException {
            return null;
        }

        @Override
        public Object immediateLoad(String entityName, Serializable id) throws HibernateException {
            return null;
        }

        @Override
        public List list(String query, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public Iterator iterate(String query, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public ScrollableResultsImplementor scroll(String query, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public ScrollableResultsImplementor scroll(Criteria criteria, ScrollMode scrollMode) {
            return null;
        }

        @Override
        public List list(Criteria criteria) {
            return null;
        }

        @Override
        public List listFilter(Object collection, String filter, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public Iterator iterateFilter(Object collection, String filter, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public EntityPersister getEntityPersister(String entityName, Object object) throws HibernateException {
            return null;
        }

        @Override
        public Object getEntityUsingInterceptor(EntityKey key) throws HibernateException {
            return null;
        }

        @Override
        public Serializable getContextEntityIdentifier(Object object) {
            return null;
        }

        @Override
        public String bestGuessEntityName(Object object) {
            return null;
        }

        @Override
        public String guessEntityName(Object entity) throws HibernateException {
            return null;
        }

        @Override
        public Object instantiate(String entityName, Serializable id) throws HibernateException {
            return null;
        }

        @Override
        public List listCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public ScrollableResultsImplementor scrollCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public List list(NativeSQLQuerySpecification spec, QueryParameters queryParameters) throws HibernateException {
            return null;
        }

        @Override
        public ScrollableResultsImplementor scroll(NativeSQLQuerySpecification spec, QueryParameters queryParameters) {
            return null;
        }

        @Override
        public int getDontFlushFromFind() {
            return 0;
        }

        @Override
        public int executeUpdate(String query, QueryParameters queryParameters) throws HibernateException {
            return 0;
        }

        @Override
        public int executeNativeUpdate(NativeSQLQuerySpecification specification, QueryParameters queryParameters) throws HibernateException {
            return 0;
        }

        @Override
        public CacheMode getCacheMode() {
            return null;
        }

        @Override
        public void setCacheMode(CacheMode cm) {
        }

        @Override
        public void setFlushMode(FlushMode flushMode) {
        }

        @Override
        public FlushModeType getFlushMode() {
            return null;
        }

        @Override
        public void setHibernateFlushMode(FlushMode flushMode) {
        }

        @Override
        public FlushMode getHibernateFlushMode() {
            return null;
        }

        @Override
        public Connection connection() {
            return null;
        }

        @Override
        public void flush() {
        }

        @Override
        public boolean isEventSource() {
            return false;
        }

        @Override
        public void afterScrollOperation() {
        }

        @Override
        public boolean shouldAutoClose() {
            return false;
        }

        @Override
        public boolean isAutoCloseSessionEnabled() {
            return false;
        }

        @Override
        public LoadQueryInfluencers getLoadQueryInfluencers() {
            return null;
        }

        @Override
        public ExceptionConverter getExceptionConverter() {
            return null;
        }
    }
}