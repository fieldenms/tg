package ua.com.fielden.platform.migration;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.security.user.User;

public class DataMigrator {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final HibernateUtil hiberUtil;
    private final EntityFactory factory;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
    private final Injector injector;
    private final DomainMetadataAnalyser dma;
    private final boolean includeDetails;
    private final IdCache cache;

    private static List<IRetriever<? extends AbstractEntity<?>>> instantiateRetrievers(final Injector injector, final Class... retrieversClasses) {
        final List<IRetriever<? extends AbstractEntity<?>>> result = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
        for (final Class<? extends IRetriever<? extends AbstractEntity<?>>> retrieverClass : retrieversClasses) {
            result.add(injector.getInstance(retrieverClass));
        }
        return result;
    }

    public DataMigrator(final Injector injector, final HibernateUtil hiberUtil, final EntityFactory factory, //
            final boolean skipValidations, final boolean includeDetails, final Class... retrieversClasses) throws Exception {
        final DateTime start = new DateTime();
        this.injector = injector;
        this.factory = factory;
        this.hiberUtil = hiberUtil;
        this.dma = new DomainMetadataAnalyser(injector.getInstance(DomainMetadata.class));
        this.retrievers.addAll(instantiateRetrievers(injector, retrieversClasses));
        this.includeDetails = includeDetails;
        final Connection conn = injector.getInstance(Connection.class);
        this.cache = new IdCache(injector.getInstance(DynamicEntityDao.class), dma);

        for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
            if (!ret.isUpdater()) {
                cache.registerCacheForType(ret.type());
            }
        }

        if (!skipValidations) {
            for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
                logger.debug("Checking props for [" + ret.getClass().getSimpleName() + "]");
                final SortedMap<String, RetrievedPropValidationError> checkResult = new RetrieverPropsValidator(dma, ret).validate();
                if (checkResult.size() > 0) {
                    logger.error("The following issues have been revealed for props in [" + ret.getClass().getSimpleName() + "]:\n " + checkResult);
                }
            }
            checkEmptyStrings(dma, conn);
            checkRequiredness(dma, conn);
            checkDataIntegrity(dma, conn);
            validateRetrievalSql(dma);
        }

        final Integer initialId = getLastId();
        final Integer finalId = batchInsert(dma, conn, initialId);
        final Period pd = new Period(start, new DateTime());

        runSql(new ArrayList<String>() {
            {
                add("UPDATE UNIQUE_ID SET NEXT_VALUE = " + finalId + " WHERE _ID = 1");
                //add("UPDATE NUMBERS SET WONOINC = COALESCE((SELECT CAST(MAX(KEY_) AS INTEGER) + 1 FROM WODET), 0) WHERE _ID = 0");
            }
        });

        System.out.println("Migration duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean validateRetrievalSqlForKeyFieldsUniqueness(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn)
            throws Exception {
        final Statement st = conn.createStatement();
        final String sql = new RetrieverSqlProducer(dma).getKeyUniquenessViolationSql(retriever);
        boolean result = false;
        try {
            logger.debug("Checking uniqueness of key data for [" + retriever.getClass().getSimpleName() + "]");
            final ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                logger.error("There are duplicates in data of [" + retriever.getClass().getSimpleName() + "].\n" + (includeDetails ? sql + "\n\n\n" : ""));
                result = true;
            }
            rs.close();
        } catch (final Exception ex) {
            logger.error("Exception while checking [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
            result = true;
        } finally {
            st.close();
        }

        return result;
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean checkRetrievalSqlForSyntaxErrors(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn)
            throws Exception {
        final Statement st = conn.createStatement();
        final String sql = new RetrieverSqlProducer(dma).getSql(retriever);
        boolean result = false;
        try {
            logger.debug("Checking sql syntax for [" + retriever.getClass().getSimpleName() + "]");
            final ResultSet rs = st.executeQuery(sql);
            rs.close();
        } catch (final Exception ex) {
            logger.error("Exception while checking syntax for [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
            result = true;
        } finally {
            st.close();
        }

        return result;
    }

    private boolean checkDataIntegrity(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
        final Map<Class<? extends AbstractEntity<?>>, String> stmts = new RetrieverDeadReferencesSeeker(dma).determineUsers(retrievers);
        final Statement st = conn.createStatement();
        boolean result = false;
        for (final Entry<Class<? extends AbstractEntity<?>>, String> entry : stmts.entrySet()) {
            try {
                final ResultSet rs = st.executeQuery(entry.getValue());
                rs.next();
                final Integer count = rs.getInt(1);
                if (count > 0) {
                    logger.error("Dead references count for entity type [" + entry.getKey().getSimpleName() + "] is [" + count + "].\n"
                            + (includeDetails ? entry.getValue() + "\n\n\n" : ""));
                }
                rs.close();
            } catch (final Exception ex) {
                logger.error("Exception while counting dead references for entity type [" + entry.getKey().getSimpleName() + "]" + ex + " SQL:\n" + entry.getValue());
                result = true;
            } finally {
            }

        }
        st.close();

        return result;
    }


    private boolean checkEmptyStrings(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
        final Set<String> stmts = new RetrieverEmptyStringsChecker(dma).getSqls(retrievers);
        final Statement st = conn.createStatement();
        boolean result = false;
        for (final String sql : stmts) {
            try {
                final ResultSet rs = st.executeQuery(sql);
                rs.next();
                final String retriever = rs.getString(1);
                final String prop = rs.getString(2);
                final Integer count = rs.getInt(3);
                if (count > 0) {
                    logger.error("Empty string reference count for property [" + prop + "] within retriever [" + retriever + "] is [" + count + "].\n"
                            + (includeDetails ? sql + "\n\n\n" : ""));
                }
                rs.close();
            } catch (final Exception ex) {
                logger.error("Exception while counting empty strings with SQL:\n" + sql);
                result = true;
            } finally {
            }

        }
        st.close();

        return result;
    }

    private boolean checkRequiredness(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
        final Set<String> stmts = new RetrieverPropsRequirednessChecker(dma).getSqls(retrievers);
        final Statement st = conn.createStatement();
        boolean result = false;
        for (final String sql : stmts) {
            try {
                final ResultSet rs = st.executeQuery(sql);
                rs.next();
                final String retriever = rs.getString(1);
                final String prop = rs.getString(2);
                final Integer count = rs.getInt(3);
                if (count > 0) {
                    logger.error("Violated requiredness records count for property [" + prop + "] within retriever [" + retriever + "] is [" + count + "].\n"
                            + (includeDetails ? sql + "\n\n\n" : ""));
                }
                rs.close();
            } catch (final Exception ex) {
                logger.error("Exception while counting records with violated requiredness with SQL:\n" + sql);
                result = true;
            } finally {
            }

        }
        st.close();

        return result;
    }

    private void runSql(final List<String> ddl) throws Exception {
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection conn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        for (final String sql : ddl) {
            final Statement st = conn.createStatement();
            st.execute(sql);
            st.close();
        }
        tr.commit();
    }

    private Integer getLastId() throws Exception {
        Integer result = null;
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        final Statement st = targetConn.createStatement();
        final ResultSet rs = st.executeQuery("SELECT NEXT_VALUE FROM UNIQUE_ID");
        rs.next();
        result = rs.getInt(1);
        st.close();
        targetConn.close();
        return result;

    }

    private void validateRetrievalSql(final DomainMetadataAnalyser dma) throws Exception {
        boolean foundErrors = false;
        final Connection conn = injector.getInstance(Connection.class);
        for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
            if (validateRetrievalSqlForKeyFieldsUniqueness(dma, ret, conn)) {
                foundErrors = true;
            }

            if (checkRetrievalSqlForSyntaxErrors(dma, ret, conn)) {
                foundErrors = true;
            }
        }
        conn.close();
        if (foundErrors) {
            logger.error("\n\n\n======== Validation detected errors. Pls consult the log file for details ==========\n\n\n");
        }
    }

    private Integer batchInsert(final DomainMetadataAnalyser dma, final Connection legacyConn, final int startingId)
            throws Exception {
        final RetrieverSqlProducer rsp = new RetrieverSqlProducer(dma);
        Integer id = startingId;
        final Map<Object, Integer> userTypeCache = cache.getCacheForType(User.class);
        final Statement legacyStmt = legacyConn.createStatement();

        for (final IRetriever<? extends AbstractEntity<?>> retriever : retrievers) {
            final ResultSet legacyRs = legacyStmt.executeQuery(rsp.getSql(retriever));

            if (retriever.isUpdater()) {
                performBatchUpdates(new RetrieverBatchUpdateStmtGenerator(dma, retriever), legacyRs);
            } else {
                id = performBatchInserts(new RetrieverBatchInsertStmtGenerator(dma, retriever), legacyRs, id, userTypeCache);
            }

            legacyRs.close();
        }
        legacyStmt.close();

        return id;
    }

    private void performBatchUpdates(final RetrieverBatchUpdateStmtGenerator rbsg, final ResultSet legacyRs) throws Exception {
        final String insertSql = rbsg.getInsertStmt();
        final List<Integer> indexFields = rbsg.produceKeyFieldsIndices();
        final DateTime start = new DateTime();
        final Map<String, List<List<Object>>> exceptions = new HashMap<>();
        final Map<Object, Integer> typeCache = cache.getCacheForType(rbsg.getRetriever().type());
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        final PreparedStatement insertStmt = targetConn.prepareStatement(insertSql);
        int batchId = 0;
        final List<List<Object>> batchValues = new ArrayList<>();

        while (legacyRs.next()) {
            batchId = batchId + 1;
            final List<Object> keyValue = new ArrayList<>();
            for (final Integer keyIndex : indexFields) {
                keyValue.add(legacyRs.getObject(keyIndex.intValue()));
            }
            final int id = typeCache.get(keyValue.size() == 1 ? keyValue.get(0) : keyValue);
            int index = 1;
            final List<Object> currTransformedValues = rbsg.transformValues(legacyRs, cache, id);
            batchValues.add(currTransformedValues);
            for (final Object value : currTransformedValues) {
                insertStmt.setObject(index, value);
                index = index + 1;
            }
            insertStmt.addBatch();

            if ((batchId % 100) == 0) {
                repeatAction(insertStmt, batchValues, exceptions);
                batchValues.clear();
                insertStmt.clearBatch();
            }
        }

        if ((batchId % 100) != 0) {
            repeatAction(insertStmt, batchValues, exceptions);
        }

        tr.commit();
        insertStmt.close();

        System.out.println(generateFinalMessage(start, rbsg.getRetriever().getClass().getSimpleName(), typeCache.size(), insertSql, exceptions));

    }

    private Integer performBatchInserts(final RetrieverBatchInsertStmtGenerator rbsg, final ResultSet legacyRs, final int startingId, final Map<Object, Integer> userTypeCache)
            throws Exception {
        final String insertSql = rbsg.getInsertStmt();
        final List<Integer> indexFields = rbsg.produceKeyFieldsIndices();
        final DateTime start = new DateTime();
        final Map<String, List<List<Object>>> exceptions = new HashMap<>();
        final Map<Object, Integer> typeCache = cache.getCacheForType(rbsg.getRetriever().type());
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        final PreparedStatement insertStmt = targetConn.prepareStatement(insertSql);
        int batchId = 0;
        final List<List<Object>> batchValues = new ArrayList<>();
        Integer id = startingId;

        while (legacyRs.next()) {
            id = id + 1;
            batchId = batchId + 1;
            final List<Object> keyValue = new ArrayList<>();
            for (final Integer keyIndex : indexFields) {
                keyValue.add(legacyRs.getObject(keyIndex.intValue()));
            }
            typeCache.put(keyValue.size() == 1 ? keyValue.get(0) : keyValue, id);
            if (rbsg.getRetriever().type().equals(User.class)) {
                userTypeCache.put(legacyRs.getObject("key"), id);
            }
            int index = 1;
            final List<Object> currTransformedValues = rbsg.transformValuesForInsert(legacyRs, cache, id);
            batchValues.add(currTransformedValues);
            for (final Object value : currTransformedValues) {
                insertStmt.setObject(index, value);
                index = index + 1;
            }
            insertStmt.addBatch();

            if ((batchId % 100) == 0) {
                repeatAction(insertStmt, batchValues, exceptions);
                batchValues.clear();
                insertStmt.clearBatch();
            }
        }

        if ((batchId % 100) != 0) {
            repeatAction(insertStmt, batchValues, exceptions);
        }

        tr.commit();
        insertStmt.close();

        System.out.println(generateFinalMessage(start, rbsg.getRetriever().getClass().getSimpleName(), typeCache.size(), insertSql, exceptions));
        return id;
    }

    private String generateFinalMessage(final DateTime start, final String retrieverName, final int entitiesCount, final String insertSql, final Map<String, List<List<Object>>> exceptions) {
        final Period pd = new Period(start, new DateTime());
        final StringBuffer sb = new StringBuffer();
        sb.append(retrieverName + " -- duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + entitiesCount + "\n");
        if (exceptions.size() > 0) {
            sb.append(StringUtils.repeat(" ", retrieverName.length()) + " -- SQL: " + insertSql + "\n");
            sb.append(StringUtils.repeat(" ", retrieverName.length()) + " -- exceptions:\n");
            for (final Entry<String, List<List<Object>>> entry : exceptions.entrySet()) {
                sb.append("                 (" + entry.getValue().size() + ") -- " + entry.getKey() + "\n");
            }
        }
        return sb.toString();
    }

    private void repeatAction(final PreparedStatement stmt, final List<List<Object>> batchValues, final Map<String, List<List<Object>>> exceptions) throws SQLException {
        try {
            stmt.executeBatch();
        } catch (final BatchUpdateException ex) {
            int updateIndex = 0;
            final List<List<Object>> existingValue = exceptions.get(ex.toString());
            if (existingValue == null) {
                exceptions.put(ex.toString(), new ArrayList<List<Object>>());
            }
            final List<List<Object>> exValues = exceptions.get(ex.toString());
            for (final int updateCount : ex.getUpdateCounts()) {
                if (updateCount != 1) {
                    exValues.add(batchValues.get(updateIndex));
                }
                updateIndex = updateIndex + 1;
            }
        }
    }
}