package ua.com.fielden.platform.migration;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.migration.MigrationUtils.generateEntityMd;
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
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.types.tuples.T2;

public class DataMigrator {
    private static final Integer BATCH_SIZE = 100; 
    
    private static final String LONG_BREAK = "\n\n\n";

    private static final Logger LOGGER = getLogger(DataMigrator.class);

    private final HibernateUtil hiberUtil;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<>();
    private final Injector injector;
    private final DomainMetadataAnalyser dma;
    private final EqlDomainMetadata eqlDomainMetadata;
    private final boolean includeDetails;
    private final IdCache cache;

    private final TimeZone utcTz = TimeZone.getTimeZone("UTC");
    private final Calendar utcCal = Calendar.getInstance(utcTz);

    public DataMigrator(
            final Injector injector,
            final HibernateUtil hiberUtil,
            final boolean skipValidations,
            final boolean includeDetails,
            final Class... retrieversClasses) throws SQLException {
        final DateTime start = new DateTime();
        this.injector = injector;
        this.hiberUtil = hiberUtil;
        final DomainMetadata eql2Md = injector.getInstance(DomainMetadata.class);
        this.eqlDomainMetadata = eql2Md.eqlDomainMetadata;
        this.dma = new DomainMetadataAnalyser(eql2Md);
        this.retrievers.addAll(instantiateRetrievers(injector, retrieversClasses));
        this.includeDetails = includeDetails;
        this.cache = new IdCache(injector.getInstance(ICompanionObjectFinder.class));

        for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
            if (!ret.isUpdater()) {
                cache.registerCacheForType(ret.type());
            }
        }

        final List<CompiledRetriever> retrieversJobs = generateRetrieversJobs(retrievers, eqlDomainMetadata);

        final Connection conn = injector.getInstance(Connection.class);
        if (!skipValidations) {
            checkEmptyStrings(dma, conn);
            checkRequiredness(dma, conn);
            checkDataIntegrity(dma, conn);
            validateRetrievalSql(dma);
        }

        final long finalId = batchInsert(retrieversJobs, conn, getNextId());
        final Period pd = new Period(start, new DateTime());

        final List<String> sql = new ArrayList<>();
        sql.add(format("ALTER SEQUENCE %s RESTART WITH %s ", ID_SEQUENCE_NAME, finalId + 1));
        runSql(sql);

        LOGGER.info("Migration duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
    }

    private static List<CompiledRetriever> generateRetrieversJobs(final List<IRetriever<? extends AbstractEntity<?>>> retrievers, final EqlDomainMetadata eqlDmd) {
        final var result = new ArrayList<CompiledRetriever>();
        for (final var retriever : retrievers) {
            try {
                final String legacySql = RetrieverSqlProducer.getSql(retriever, true);
                final var retResultFieldsIndices = new HashMap<String, Integer>();
                int index = 1;
                for (final var propPath : retriever.resultFields().keySet()) {
                    retResultFieldsIndices.put(propPath, index);
                    index = index + 1;
                }
                final var emd = eqlDmd.entityPropsMetadata().get(retriever.type());
                  
                if (retriever.isUpdater()) {
                    result.add(CompiledRetriever.forUpdate(retriever, legacySql, new TargetDataUpdate(retriever.type(), retResultFieldsIndices, generateEntityMd(emd.typeInfo.tableName, emd.props()))));    
                } else {
                    result.add(CompiledRetriever.forInsert(retriever, legacySql, new TargetDataInsert(retriever.type(), retResultFieldsIndices, generateEntityMd(emd.typeInfo.tableName, emd.props()))));    
                }
            } catch (final Exception ex) {
                throw new DataMigrationException("Errors while compiling retriever [" + retriever.getClass().getSimpleName() + "].", ex);
            }
        }

        return result;
    }
    
    private static List<IRetriever<? extends AbstractEntity<?>>> instantiateRetrievers(final Injector injector, final Class... retrieversClasses) {
        final var result = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
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
        final var sql = RetrieverSqlProducer.getKeyUniquenessViolationSql(retriever, dma);
        boolean result = false;
        try (final var st = conn.createStatement()) {
            LOGGER.debug("Checking uniqueness of key data for [" + retriever.getClass().getSimpleName() + "]");
            try (final var rs = st.executeQuery(sql)) {
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
        final var sql = RetrieverSqlProducer.getSql(retriever, true);
        boolean result = false;
        LOGGER.debug("Checking sql syntax for [" + retriever.getClass().getSimpleName() + "]");
        try (final var st = conn.createStatement(); final var rs = st.executeQuery(sql)) {
            LOGGER.debug("SQL syntax is valid.");
        } catch (final Exception ex) {
            LOGGER.error("Exception while checking syntax for [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
            result = true;
        }
        return result;
    }

    private boolean checkDataIntegrity(final DomainMetadataAnalyser dma, final Connection conn) {
        final var stmts = new RetrieverDeadReferencesSeeker(dma).determineUsers(retrievers);
        boolean result = false;
        for (final var entry : stmts.entrySet()) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(entry.getValue())) {
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
        final var stmts = new RetrieverEmptyStringsChecker(dma).getSqls(retrievers);
        boolean result = false;
        for (final var sql : stmts) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(sql)) {
                rs.next();
                final var retriever = rs.getString(1);
                final var prop = rs.getString(2);
                final var count = rs.getInt(3);
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
        final var stmts = new RetrieverPropsRequirednessChecker(dma).getSqls(retrievers);
        boolean result = false;
        for (final var sql : stmts) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(sql)) {
                rs.next();
                final var retriever = rs.getString(1);
                final var prop = rs.getString(2);
                final var count = rs.getInt(3);
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
        final var tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        hiberUtil.getSessionFactory().getCurrentSession().doWork(conn -> {
            for (final var sql : ddl) {
                try (final Statement st = conn.createStatement()) {
                    st.execute(sql);
                }
            }
        });
        tr.commit();
    }

    private long getNextId() throws SQLException {
        final var tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final long result = nextIdValue(ID_SEQUENCE_NAME, hiberUtil.getSessionFactory().getCurrentSession());
        tr.commit();
        return result;
    }

    private void validateRetrievalSql(final DomainMetadataAnalyser dma) {
        boolean foundErrors = false;
        final var conn = injector.getInstance(Connection.class);
        for (final var ret : retrievers) {
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

    private long batchInsert(final List<CompiledRetriever> compiledRetrievers, final Connection legacyConn, final long startingId) throws SQLException {
        final AtomicLong id = new AtomicLong(startingId);
        for (final var cr : compiledRetrievers) {
            try (final var legacyStmt = legacyConn.createStatement(); final var legacyRs = legacyStmt.executeQuery(cr.legacySql)) {
                final var retrieverName = cr.retriever.getClass().getSimpleName();
                LOGGER.info("Executing compiled retriever " + retrieverName);
                try {
                    final Function<TargetDataUpdate, Optional<Long>> updater = (tdu) -> {
                        performBatchUpdates(tdu, legacyRs, retrieverName);
                        return empty();
                    };
                    final Function<TargetDataInsert, Optional<Long>> inserter = (tdi) -> {
                        return of(performBatchInserts(tdi, legacyRs, retrieverName, id.get()));
                    };

                    cr.exec(updater, inserter).ifPresent(newId -> id.set(newId));
                } catch (final Exception ex) {
                    throw new DataMigrationException("Errors while processing " + retrieverName, ex);
                }
            }
        }

        return id.get();
    }

    private void performBatchUpdates(final TargetDataUpdate tdu, final ResultSet legacyRs, final String retrieverName) {
        final var start = new DateTime();
        final var exceptions = new HashMap<String, List<List<Object>>>();
        final var typeCache = cache.getCacheForType(tdu.retrieverEntityType);
        final var tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        hiberUtil.getSessionFactory().getCurrentSession().doWork(targetConn -> {
            try (final var insertStmt = targetConn.prepareStatement(tdu.updateStmt)) {
                int batchId = 0;
                final var batchValues = new ArrayList<List<Object>>();

                while (legacyRs.next()) {
                    batchId = batchId + 1;
                    final var keyValue = new ArrayList<>();
                    for (final Integer keyIndex : tdu.keyIndices) {
                        keyValue.add(legacyRs.getObject(keyIndex.intValue()));
                    }
                    final Object key = keyValue.size() == 1 ? keyValue.get(0) : keyValue;
                    final Long idObject = typeCache.get(key);
                    final long id = idObject;
                    int index = 1;
                    final var currTransformedValues = tdu.transformValuesForUpdate(legacyRs, cache, id);
                    batchValues.add(currTransformedValues);
                    for (final Object value : currTransformedValues) {
                        insertStmt.setObject(index, value);
                        index = index + 1;
                    }
                    insertStmt.addBatch();

                    if ((batchId % BATCH_SIZE) == 0) {
                        repeatAction(insertStmt, batchValues, exceptions);
                        batchValues.clear();
                        insertStmt.clearBatch();
                    }
                }

                if ((batchId % BATCH_SIZE) != 0) {
                    repeatAction(insertStmt, batchValues, exceptions);
                }
            }
        });
        tr.commit();
        LOGGER.info(generateFinalMessage(start, retrieverName, typeCache.size(), tdu.updateStmt, exceptions));
    }

    private long performBatchInserts(final TargetDataInsert tdi, final ResultSet legacyRs, final String retrieverName, final long startingId) {
        final var start = new DateTime();
        final var exceptions = new HashMap<String, List<List<Object>>>();
        final var typeCache = cache.getCacheForType(tdi.retrieverEntityType);
        final var tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();

        final long idToReturn = hiberUtil.getSessionFactory().getCurrentSession().doReturningWork(targetConn -> {
            try (final var insertStmt = targetConn.prepareStatement(tdi.insertStmt)) {
                int batchId = 0;
                final var batchValues = new ArrayList<List<Object>>();
                Long id = startingId;
                while (legacyRs.next()) {
                    id = id + 1;
                    batchId = batchId + 1;
                    final List<Object> keyValue = new ArrayList<>();
                    for (final Integer keyIndex : tdi.keyIndices) {
                        keyValue.add(legacyRs.getObject(keyIndex.intValue()));
                    }
                    typeCache.put(keyValue.size() == 1 ? keyValue.get(0) : keyValue, id);

                    int index = 1;
                    final var currTransformedValues = tdi.transformValuesForInsert(legacyRs, cache, id);
                    batchValues.add(currTransformedValues.stream().map(f -> f._1).collect(toList()));
                    for (final var t2 : currTransformedValues) {
                        transformIfUtcValueAndSet(t2._2, insertStmt, index, t2._1);
                        index = index + 1;
                    }
                    insertStmt.addBatch();

                    if ((batchId % BATCH_SIZE) == 0) {
                        repeatAction(insertStmt, batchValues, exceptions);
                        batchValues.clear();
                        insertStmt.clearBatch();
                    }
                }

                if ((batchId % BATCH_SIZE) != 0) {
                    repeatAction(insertStmt, batchValues, exceptions);
                }

                LOGGER.info(generateFinalMessage(start, retrieverName, typeCache.size(), tdi.insertStmt, exceptions));
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
    private void transformIfUtcValueAndSet(final boolean hasUtcType, final PreparedStatement insertStmt, final int index, final Object value) throws SQLException {
        if (hasUtcType) {
            final Timestamp ts = (Timestamp) value;
            insertStmt.setTimestamp(index, ts, utcCal);
        } else {
            insertStmt.setObject(index, value);
        }
    }

    private String generateFinalMessage(final DateTime start, final String retrieverName, final int entitiesCount, final String insertSql, final Map<String, List<List<Object>>> exceptions) {
        final Period pd = new Period(start, new DateTime());
        final StringBuilder sb = new StringBuilder();
        sb.append("    " + retrieverName + " -- duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + entitiesCount);
        if (!exceptions.isEmpty()) {
            sb.append("\n" + StringUtils.repeat(" ", retrieverName.length()) + " -- SQL: " + insertSql);
            sb.append("\n" + StringUtils.repeat(" ", retrieverName.length()) + " -- exceptions:");
            for (final Entry<String, List<List<Object>>> entry : exceptions.entrySet()) {
                sb.append("\n" + "                 (" + entry.getValue().size() + ") -- " + entry.getKey());
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