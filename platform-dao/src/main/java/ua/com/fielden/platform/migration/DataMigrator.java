package ua.com.fielden.platform.migration;

import static java.lang.String.format;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

public class DataMigrator {
    private static final String LONG_BREAK = "\n\n\n";

    private final Logger logger = Logger.getLogger(this.getClass());

    private final HibernateUtil hiberUtil;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<>();
    private final Injector injector;
    private final DomainMetadataAnalyser dma;
    private final boolean includeDetails;
    private final IdCache cache;

    private final TimeZone utcTz = TimeZone.getTimeZone("UTC");
    private final Calendar utcCal = Calendar.getInstance(utcTz);

    public DataMigrator(final Injector injector, final HibernateUtil hiberUtil,
            final boolean skipValidations, final boolean includeDetails, final Class... retrieversClasses) throws Exception {
        final DateTime start = new DateTime();
        this.injector = injector;
        this.hiberUtil = hiberUtil;
        dma = new DomainMetadataAnalyser(injector.getInstance(DomainMetadata.class));
        retrievers.addAll(instantiateRetrievers(injector, retrieversClasses));
        this.includeDetails = includeDetails;
        final Connection conn = injector.getInstance(Connection.class);
        cache = new IdCache(injector.getInstance(ICompanionObjectFinder.class), dma);

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

        final List<String> sql = new ArrayList<>();
        sql.add("UPDATE UNIQUE_ID SET NEXT_VALUE = " + finalId + " WHERE _ID = 1");
        runSql(sql);

        System.out.println("Migration duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
    }

    private static List<IRetriever<? extends AbstractEntity<?>>> instantiateRetrievers(final Injector injector, final Class... retrieversClasses) {
        final List<IRetriever<? extends AbstractEntity<?>>> result = new ArrayList<>();
        for (final Class<? extends IRetriever<? extends AbstractEntity<?>>> retrieverClass : retrieversClasses) {
            result.add(injector.getInstance(retrieverClass));
        }
        return result;
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean validateRetrievalSqlForKeyFieldsUniqueness(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn) {
        final String sql = new RetrieverSqlProducer(dma).getKeyUniquenessViolationSql(retriever);
        boolean result = false;
        try (final Statement st = conn.createStatement()) {
            logger.debug("Checking uniqueness of key data for [" + retriever.getClass().getSimpleName() + "]");
            try (final ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    logger.error(format("There are duplicates in data of [%s].\n"
                            + "%s", retriever.getClass().getSimpleName(), (includeDetails ? sql + LONG_BREAK : "")));
                    result = true;
                }
            }
        } catch (final Exception ex) {
            logger.error(format("Exception while checking [%s]%s SQL:\n"
                    + "%s", retriever.getClass().getSimpleName(), ex, sql));
            result = true;
        }

        return result;
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean checkRetrievalSqlForSyntaxErrors(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn) {
        
        final String sql = new RetrieverSqlProducer(dma).getSql(retriever);
        boolean result = false;
        logger.debug("Checking sql syntax for [" + retriever.getClass().getSimpleName() + "]");
        try (final Statement st = conn.createStatement();
             final ResultSet rs = st.executeQuery(sql)) {
        } catch (final Exception ex) {
            logger.error("Exception while checking syntax for [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
            result = true;
        }
        return result;
    }

    private boolean checkDataIntegrity(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
        final Map<Class<? extends AbstractEntity<?>>, String> stmts = new RetrieverDeadReferencesSeeker(dma).determineUsers(retrievers);
        boolean result = false;
        for (final Entry<Class<? extends AbstractEntity<?>>, String> entry : stmts.entrySet()) {
            try (final Statement st = conn.createStatement();
                 final ResultSet rs = st.executeQuery(entry.getValue())) {
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.error("Dead references count for entity type [" + entry.getKey().getSimpleName() + "] is [" + rs.getInt(1) + "].\n" + (includeDetails ? entry.getValue() + LONG_BREAK : ""));
                }
            } catch (final Exception ex) {
                logger.error("Exception while counting dead references for entity type [" + entry.getKey().getSimpleName() + "]" + ex + " SQL:\n" + entry.getValue());
                result = true;
            }
        }
        return result;
    }


    private boolean checkEmptyStrings(final DomainMetadataAnalyser dma, final Connection conn) throws SQLException {
        final Set<String> stmts = new RetrieverEmptyStringsChecker(dma).getSqls(retrievers);
            boolean result = false;
            for (final String sql : stmts) {
                try (final Statement st = conn.createStatement();
                     final ResultSet rs = st.executeQuery(sql)) {
                    rs.next();
                    final String retriever = rs.getString(1);
                    final String prop = rs.getString(2);
                    final Integer count = rs.getInt(3);
                    if (count > 0) {
                        logger.error("Empty string reference count for property [" + prop + "] within retriever [" + retriever + "] is [" + count + "].\n" + (includeDetails ? sql + LONG_BREAK : ""));
                    }
                    
                } catch (final SQLException ex) {
                    logger.error("Exception while counting empty strings with SQL:\n" + sql);
                    result = true;
                }
            }
            return result;
    }

    private boolean checkRequiredness(final DomainMetadataAnalyser dma, final Connection conn) throws SQLException {
        final Set<String> stmts = new RetrieverPropsRequirednessChecker(dma).getSqls(retrievers);
            boolean result = false;
            for (final String sql : stmts) {
                try (final Statement st = conn.createStatement();
                     final ResultSet rs = st.executeQuery(sql)) {
                    rs.next();
                    final String retriever = rs.getString(1);
                    final String prop = rs.getString(2);
                    final Integer count = rs.getInt(3);
                    if (count > 0) {
                        logger.error("Violated requiredness records count for property [" + prop + "] within retriever [" + retriever + "] is [" + count + "].\n"
                                + (includeDetails ? sql + LONG_BREAK : ""));
                    }
                } catch (final SQLException ex) {
                    logger.error("Exception while counting records with violated requiredness with SQL:\n" + sql);
                    result = true;
                } 
            }
            return result;
    }

    private void runSql(final List<String> ddl) throws Exception {
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection conn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        for (final String sql : ddl) {
            try (final Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        }
        tr.commit();
    }

    private Integer getLastId() throws Exception {
        Integer result = null;
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        try (final Statement st = targetConn.createStatement();
             final ResultSet rs = st.executeQuery("SELECT NEXT_VALUE FROM UNIQUE_ID")) {
            rs.next();
            result = rs.getInt(1);
        }
        tr.commit();
        return result;

    }

    private void validateRetrievalSql(final DomainMetadataAnalyser dma) {
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
        
        if (foundErrors) {
            logger.error("\n\n\n======== Validation detected errors. Pls consult the log file for details ==========\n\n\n");
        }
    }

    private Integer batchInsert(final DomainMetadataAnalyser dma, final Connection legacyConn, final int startingId)
            throws Exception {
        final RetrieverSqlProducer rsp = new RetrieverSqlProducer(dma);
        Integer id = startingId;
            for (final IRetriever<? extends AbstractEntity<?>> retriever : retrievers) {
                try (final Statement legacyStmt = legacyConn.createStatement();
                     final ResultSet legacyRs = legacyStmt.executeQuery(rsp.getSql(retriever))) {
                    if (retriever.isUpdater()) {
                        performBatchUpdates(new RetrieverBatchUpdateStmtGenerator(dma, retriever), legacyRs);
                    } else {
                        id = performBatchInserts(new RetrieverBatchInsertStmtGenerator(dma, retriever), legacyRs, id);
                    }

                }
            }

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
        try (final PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {
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
        }
        System.out.println(generateFinalMessage(start, rbsg.getRetriever().getClass().getSimpleName(), typeCache.size(), insertSql, exceptions));

    }

    private Integer performBatchInserts(final RetrieverBatchInsertStmtGenerator rbsg, final ResultSet legacyRs, final int startingId)
            throws Exception {
        final String insertSql = rbsg.getInsertStmt();
        final List<Integer> indexFields = rbsg.produceKeyFieldsIndices();
        final DateTime start = new DateTime();
        final Map<String, List<List<Object>>> exceptions = new HashMap<>();
        final Map<Object, Integer> typeCache = cache.getCacheForType(rbsg.getRetriever().type());
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();
        try (final PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {
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
    
                int index = 1;
                final List<Object> currTransformedValues = rbsg.transformValuesForInsert(legacyRs, cache, id);
                batchValues.add(currTransformedValues);
                for (final Object value : currTransformedValues) {
                    transformIfUtcValueAndSet(rbsg.insertFields.get(index - 1), insertStmt, index, value);
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
            
            System.out.println(generateFinalMessage(start, rbsg.getRetriever().getClass().getSimpleName(), typeCache.size(), insertSql, exceptions));
            return id;
        }
    }

    /**
     * Handles assignment of date/time fields for properties with UTC markers.
     * 
     * @param propMetadata
     * @param insertStmt
     * @param index
     * @param value
     * @throws SQLException
     */
    private void transformIfUtcValueAndSet(final PropertyMetadata propMetadata, final PreparedStatement insertStmt, final int index, final Object value) throws SQLException {
        if (propMetadata.getHibTypeAsUserType() instanceof IUtcDateTimeType) {
            final Timestamp ts = (Timestamp) value;
            insertStmt.setTimestamp(index, ts, utcCal);
        } else {
            insertStmt.setObject(index, value);
        }
    }

    private String generateFinalMessage(final DateTime start, final String retrieverName, final int entitiesCount, final String insertSql, final Map<String, List<List<Object>>> exceptions) {
        final Period pd = new Period(start, new DateTime());
        final StringBuilder sb = new StringBuilder();
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