package ua.com.fielden.platform.migration;

import static java.lang.String.format;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;

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

    private static final Logger LOGGER = Logger.getLogger(DataMigrator.class);

    private final HibernateUtil hiberUtil;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<>();
    private final Injector injector;
    private final DomainMetadataAnalyser dma;
    private final boolean includeDetails;
    private final IdCache cache;

    private final TimeZone utcTz = TimeZone.getTimeZone("UTC");
    private final Calendar utcCal = Calendar.getInstance(utcTz);

    public DataMigrator(final Injector injector, final HibernateUtil hiberUtil,
            final boolean skipValidations, final boolean includeDetails, final Class... retrieversClasses) throws SQLException {
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
                LOGGER.debug("Checking props for [" + ret.getClass().getSimpleName() + "]");
                final SortedMap<String, RetrievedPropValidationError> checkResult = new RetrieverPropsValidator(dma, ret).validate();
                if (checkResult.size() > 0) {
                    LOGGER.error("The following issues have been revealed for props in [" + ret.getClass().getSimpleName() + "]:\n " + checkResult);
                }
            }
            checkEmptyStrings(dma, conn);
            checkRequiredness(dma, conn);
            checkDataIntegrity(dma, conn);
            validateRetrievalSql(dma);
        }

        final long finalId = batchInsert(dma, conn, getNextId());
        final Period pd = new Period(start, new DateTime());

        final List<String> sql = new ArrayList<>();
        sql.add(String.format("ALTER SEQUENCE %s RESTART WITH %s ", ID_SEQUENCE_NAME, finalId + 1));
        runSql(sql);

        LOGGER.info("Migration duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
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
            LOGGER.debug("Checking uniqueness of key data for [" + retriever.getClass().getSimpleName() + "]");
            try (final ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    LOGGER.error(format("There are duplicates in data of [%s].\n"
                            + "%s", retriever.getClass().getSimpleName(), includeDetails ? sql + LONG_BREAK : ""));
                    result = true;
                }
            }
        } catch (final Exception ex) {
            LOGGER.error(format("Exception while checking [%s]%s SQL:\n"
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
        LOGGER.debug("Checking sql syntax for [" + retriever.getClass().getSimpleName() + "]");
        try (final Statement st = conn.createStatement();
             final ResultSet rs = st.executeQuery(sql)) {
            LOGGER.debug("SQL syntax is valid.");
        } catch (final Exception ex) {
            LOGGER.error("Exception while checking syntax for [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
            result = true;
        }
        return result;
    }

    private boolean checkDataIntegrity(final DomainMetadataAnalyser dma, final Connection conn) {
        final Map<Class<? extends AbstractEntity<?>>, String> stmts = new RetrieverDeadReferencesSeeker(dma).determineUsers(retrievers);
        boolean result = false;
        for (final Entry<Class<? extends AbstractEntity<?>>, String> entry : stmts.entrySet()) {
            try (final Statement st = conn.createStatement();
                 final ResultSet rs = st.executeQuery(entry.getValue())) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.error(format("Dead references count for entity type [%s] is [%s].\n"
                            + "%s", entry.getKey().getSimpleName(), rs.getInt(1), includeDetails ? entry.getValue() + LONG_BREAK : ""));
                }
            } catch (final Exception ex) {
                LOGGER.error(format("Exception while counting dead references for entity type [%s]%s SQL:\n"
                        + "%s", entry.getKey().getSimpleName(), ex, entry.getValue()));
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
                    LOGGER.error(format("Empty string reference count for property [%s] within retriever [%s] is [%s].\n"
                            + "%s", prop, retriever, count, includeDetails ? sql + LONG_BREAK : ""));
                }

            } catch (final SQLException ex) {
                LOGGER.error("Exception while counting empty strings with SQL:\n" + sql, ex);
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
                    LOGGER.error(format("Violated requiredness records count for property [%s] within retriever [%s] is [%s].\n"
                            + "%s", prop, retriever, count, includeDetails ? sql + LONG_BREAK : ""));
                }
            } catch (final SQLException ex) {
                LOGGER.error("Exception while counting records with violated requiredness with SQL:\n" + sql, ex);
                result = true;
            }
        }
        return result;
    }

    private void runSql(final List<String> ddl) throws SQLException {
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        hiberUtil.getSessionFactory().getCurrentSession().doWork(conn -> {
            for (final String sql : ddl) {
                try (final Statement st = conn.createStatement()) {
                    st.execute(sql);
                }
            }
        });
        tr.commit();
    }

    private long getNextId() throws SQLException {
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final long result = nextIdValue(ID_SEQUENCE_NAME, hiberUtil.getSessionFactory().getCurrentSession());
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
            LOGGER.error("\n\n\n======== Validation detected errors. Pls consult the log file for details ==========\n\n\n");
        }
    }

    private long batchInsert(final DomainMetadataAnalyser dma, final Connection legacyConn, final long startingId) throws SQLException {
        final RetrieverSqlProducer rsp = new RetrieverSqlProducer(dma);
        long id = startingId;
            for (final IRetriever<? extends AbstractEntity<?>> retriever : retrievers) {
                try (final Statement legacyStmt = legacyConn.createStatement();
                    final ResultSet legacyRs = legacyStmt.executeQuery(rsp.getSql(retriever))) {
                // retriever.getClass().getName().contains("StPoInventoryLineRetriever");

                System.out.println("Processing retriever " + retriever);
                    if (retriever.isUpdater()) {
                        performBatchUpdates(new RetrieverBatchUpdateStmtGenerator(dma, retriever), legacyRs);
                    } else {
                        id = performBatchInserts(new RetrieverBatchInsertStmtGenerator(dma, retriever), legacyRs, id);
                    }

                }
            }

        return id;
    }

    private void performBatchUpdates(final RetrieverBatchUpdateStmtGenerator rbsg, final ResultSet legacyRs) throws SQLException {
        final String insertSql = rbsg.getInsertStmt();
        final List<Integer> indexFields = rbsg.produceKeyFieldsIndices();
        final DateTime start = new DateTime();
        final Map<String, List<List<Object>>> exceptions = new HashMap<>();
        final Map<Object, Long> typeCache = cache.getCacheForType(rbsg.getRetriever().type());
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        hiberUtil.getSessionFactory().getCurrentSession().doWork(targetConn -> {
            try (final PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {
                int batchId = 0;
                final List<List<Object>> batchValues = new ArrayList<>();

                while (legacyRs.next()) {
                    batchId = batchId + 1;
                    final List<Object> keyValue = new ArrayList<>();
                    for (final Integer keyIndex : indexFields) {
                        keyValue.add(legacyRs.getObject(keyIndex.intValue()));
                    }
                if (keyValue == null) {
                    System.out.println("keyValue == null");
                }
                final Object key = keyValue.size() == 1 ? keyValue.get(0) : keyValue;
                if (key == null) {
                    System.out.println("key == null");
                }
                final Long idObject = typeCache.get(key);
                if (idObject == null) {
                    System.out.println("idObject == null");
                } else {
                    final long id = idObject;
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
            }

                if ((batchId % 100) != 0) {
                    repeatAction(insertStmt, batchValues, exceptions);
                }
            }
        });
        tr.commit();
        LOGGER.info(generateFinalMessage(start, rbsg.getRetriever().getClass().getSimpleName(), typeCache.size(), insertSql, exceptions));

    }

    private long performBatchInserts(final RetrieverBatchInsertStmtGenerator rbsg, final ResultSet legacyRs, final long startingId) throws SQLException {
        final String insertSql = rbsg.getInsertStmt();
        final List<Integer> indexFields = rbsg.produceKeyFieldsIndices();
        final DateTime start = new DateTime();
        final Map<String, List<List<Object>>> exceptions = new HashMap<>();
        final Map<Object, Long> typeCache = cache.getCacheForType(rbsg.getRetriever().type());
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();

        final long idToReturn = hiberUtil.getSessionFactory().getCurrentSession().doReturningWork(targetConn -> {
            try (final PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {
                int batchId = 0;
                final List<List<Object>> batchValues = new ArrayList<>();
                Long id = startingId;

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

                LOGGER.info(generateFinalMessage(start, rbsg.getRetriever().getClass().getSimpleName(), typeCache.size(), insertSql, exceptions));
                return id;
            }
        });
        tr.commit();
        return idToReturn;
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
            LOGGER.warn(ex);
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