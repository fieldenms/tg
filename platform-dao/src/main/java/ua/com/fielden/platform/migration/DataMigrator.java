package ua.com.fielden.platform.migration;

import com.google.inject.Injector;
import jakarta.inject.Provider;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;
import static ua.com.fielden.platform.utils.StreamUtils.enumerate;

public class DataMigrator {

    public static final String ERR_WHILE_EXECUTING_RETRIEVERS = "An error occurred while executing retriever [%s]",
                               ERR_TO_COMPILE_RETRIEVER = "Failed to compile retriever [%s].";

    private static final Integer BATCH_SIZE = 100;

    private static final Logger LOGGER = getLogger();

    private static final TimeZone utcTz = TimeZone.getTimeZone("UTC");
    private static final Calendar utcCal = Calendar.getInstance(utcTz);

    private final Provider<TransactionalExecution> transactionalExecutionProvider;
    private final IdCache cache;
    private final MigrationUtils utils;

    public DataMigrator(
            final Injector injector,
            final boolean skipValidations,
            final boolean includeDetails,
            final Class<? extends IRetriever<? extends AbstractEntity<?>>>... retrieverTypes) throws SQLException
    {
        final DateTime start = new DateTime();
        this.transactionalExecutionProvider = injector.getProvider(TransactionalExecution.class);
        this.utils = injector.getInstance(MigrationUtils.class);
        this.cache = new IdCache();

        final var retrievers = instantiateRetrievers(injector, retrieverTypes);

        for (final var ret : retrievers) {
            if (!ret.isUpdater()) {
                cache.registerCacheForType(ret.type());
            }
        }

        final var sqlProducer = injector.getInstance(RetrieverSqlProducer.class);
        final List<CompiledRetriever> compiledRetrievers = compileRetrievers(retrievers, utils, sqlProducer);

        final Connection legacyConn = injector.getInstance(Connection.class);
        if (!skipValidations) {
            injector.getInstance(DataValidator.class).performValidations(legacyConn, includeDetails, compiledRetrievers);
        }
        final long lastId = batchInsert(compiledRetrievers, legacyConn, getNextId());
        final var period = new Period(start, new DateTime());

        runSql(List.of(format("ALTER SEQUENCE %s RESTART WITH %s ", ID_SEQUENCE_NAME, lastId + 1)));

        LOGGER.info(() -> "Migration duration: %s m %s s %s ms".formatted(period.getMinutes(), period.getSeconds(), period.getMillis()));
    }

    /// Prints out retrievers grouped by their entity types (only those that have more than one retriever per type).
    ///
    private static void printRetrieversScheme(final List<CompiledRetriever> retrieversJobs) {
        final var allRetrieverByType = retrieversJobs.stream().collect(Collectors.groupingBy(CompiledRetriever::getType));

        for (final Entry<? extends Class<? extends AbstractEntity<?>>, List<CompiledRetriever>> typeAndItsRetrievers : allRetrieverByType.entrySet()) {
            if (typeAndItsRetrievers.getValue().size() > 1) {
                LOGGER.info(() -> " == " + typeAndItsRetrievers.getKey().getSimpleName());
                for (final CompiledRetriever compiledRetriever : typeAndItsRetrievers.getValue()) {
                    LOGGER.info(() -> "        " + (compiledRetriever.isUpdater() ? "U" : " ") + " "
                                      + compiledRetriever.retriever().getClass().getSimpleName());
                }
            }
        }
    }

    private static List<CompiledRetriever> compileRetrievers(
            final List<? extends IRetriever<? extends AbstractEntity<?>>> retrievers,
            final MigrationUtils utils,
            final RetrieverSqlProducer sqlProducer)
    {
        return retrievers.stream()
                .map(retriever -> {
                    try {
                        final String legacySql = sqlProducer.getSql(retriever);
                        final var md = utils.generateEntityMd(retriever.type());

                        final var resultFieldIndices = enumerate(retriever.resultFields().keySet().stream(), 1, T2::t2)
                                .collect(toMap(t2 -> t2._1, t2 -> t2._2));
                        if (retriever.isUpdater()) {
                            return CompiledRetriever.forUpdate(retriever, legacySql, utils.targetDataUpdate(retriever.type(), resultFieldIndices, md), md);
                        } else {
                            return CompiledRetriever.forInsert(retriever, legacySql, utils.targetDataInsert(retriever.type(), resultFieldIndices, md), md);
                        }
                    }
                    catch (final Exception ex) {
                        throw new DataMigrationException(ERR_TO_COMPILE_RETRIEVER.formatted(retriever.getClass().getSimpleName()), ex);
                    }
                })
                .toList();
    }

    private static List<? extends IRetriever<? extends AbstractEntity<?>>> instantiateRetrievers(
            final Injector injector,
            final Class<? extends IRetriever<? extends AbstractEntity<?>>>... retrieverTypes)
    {
        return Arrays.stream(retrieverTypes).map(injector::getInstance).toList();
    }

    private void runSql(final List<String> statements) {
        transactionalExecutionProvider.get().exec(conn -> {
            for (final var sql : statements) {
                try (final var st = conn.createStatement()) {
                    st.execute(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private long getNextId() {
        return transactionalExecutionProvider.get()
                .execWithSession(sessionEnabled -> nextIdValue(ID_SEQUENCE_NAME, sessionEnabled.getSession()));
    }

    private long batchInsert(final List<CompiledRetriever> retrievers, final Connection legacyConn, final long firstId) throws SQLException {
        final var id = new AtomicLong(firstId);

        for (final var ret : retrievers) {
            final var retrieverName = ret.retriever().getClass().getSimpleName();
            try (final var legacyStmt = legacyConn.createStatement(); final var legacyRs = legacyStmt.executeQuery(ret.legacySql())) {
                LOGGER.info(() -> "Executing retriever [%s]".formatted(retrieverName));
                final Function<TargetDataUpdate, Optional<Long>> updater = tdu -> {
                    performBatchUpdates(tdu, legacyRs, retrieverName);
                    return empty();
                };
                final Function<TargetDataInsert, Optional<Long>> inserter = tdi ->
                    of(performBatchInserts(tdi, legacyRs, retrieverName, id.get()));

                ret.exec(updater, inserter).ifPresent(id::set);
            } catch (final Exception ex) {
                throw new DataMigrationException(ERR_WHILE_EXECUTING_RETRIEVERS.formatted(retrieverName), ex);
            }
        }

        return id.get();
    }

    private void performBatchUpdates(final TargetDataUpdate tdu, final ResultSet legacyRs, final String retrieverName) {
        final var start = new DateTime();
        final var exceptions = new HashMap<String, List<List<Object>>>();
        final var typeCache = utils.cacheForType(cache, tdu.entityType());

        transactionalExecutionProvider.get().exec(targetConn -> {
            try (final var insertStmt = targetConn.prepareStatement(tdu.updateStmt())) {
                int batchId = 0;
                final var batchValues = new ArrayList<List<Object>>();

                while (legacyRs.next()) {
                    batchId = batchId + 1;
                    final var keyValue = new ArrayList<>();
                    for (final Integer keyIndex : tdu.keyIndices()) {
                        keyValue.add(legacyRs.getObject(keyIndex));
                    }
                    final Object key = keyValue.size() == 1 ? keyValue.getFirst() : keyValue;
                    final Long idObject = typeCache.get(key);
                    if (idObject == null) {
                        LOGGER.warn(() -> "           !!! can't find id for " + tdu.entityType().getSimpleName() + " with key: [" + key + "]");
                    } else {
                        final long id = idObject;
                        int index = 1;
                        final var currTransformedValues = utils.transformValuesForUpdate(tdu, legacyRs, cache, id);
                        batchValues.add(currTransformedValues);
                        for (final Object value : currTransformedValues) {
                            insertStmt.setObject(index, value);
                            index = index + 1;
                        }
                        insertStmt.addBatch();
                    }

                    if (batchId % BATCH_SIZE == 0) {
                        repeatAction(insertStmt, batchValues, exceptions);
                        batchValues.clear();
                        insertStmt.clearBatch();
                    }
                }

                if (batchId % BATCH_SIZE != 0) {
                    repeatAction(insertStmt, batchValues, exceptions);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        LOGGER.info(generateFinalMessage(start, retrieverName, typeCache.size(), tdu.updateStmt(), exceptions));
    }

    private long performBatchInserts(final TargetDataInsert tdi, final ResultSet legacyRs, final String retrieverName, final long startingId) {
        final var start = new DateTime();
        final var exceptions = new HashMap<String, List<List<Object>>>();
        final var typeCache = utils.cacheForType(cache, tdi.entityType());

        return transactionalExecutionProvider.get().execFn(targetConn -> {
            try (final var insertStmt = targetConn.prepareStatement(tdi.insertStmt())) {
                int batchId = 0;
                final var batchValues = new ArrayList<List<Object>>();
                long id = startingId;
                while (legacyRs.next()) {
                    id = id + 1;
                    batchId = batchId + 1;
                    final var keyValues = new ArrayList<>(tdi.keyIndices().size());
                    for (final var keyIndex : tdi.keyIndices()) {
                        keyValues.add(legacyRs.getObject(keyIndex));
                    }
                    final var cacheKey = keyValues.size() == 1 ? keyValues.getFirst() : keyValues;
                    if (typeCache.containsKey(cacheKey)) {
                        continue;
                    }
                    typeCache.put(cacheKey, id);

                    int index = 1;
                    final var currTransformedValues = utils.transformValuesForInsert(tdi, legacyRs, cache, id);
                    batchValues.add(currTransformedValues.stream().map(f -> f._1).toList());
                    for (final var t2 : currTransformedValues) {
                        transformIfUtcValueAndSet(t2._2, insertStmt, index, t2._1);
                        index = index + 1;
                    }
                    insertStmt.addBatch();

                    if (batchId % BATCH_SIZE == 0) {
                        repeatAction(insertStmt, batchValues, exceptions);
                        batchValues.clear();
                        insertStmt.clearBatch();
                    }
                }

                if (batchId % BATCH_SIZE != 0) {
                    repeatAction(insertStmt, batchValues, exceptions);
                }

                LOGGER.info(() -> generateFinalMessage(start, retrieverName, typeCache.size(), tdi.insertStmt(), exceptions));
                return id;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /// Handles assignment of date/time fields for properties with UTC markers.
    ///
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
            exceptions.computeIfAbsent(ex.toString(), k -> new ArrayList<List<Object>>());
            final var exValues = exceptions.get(ex.toString());
            for (final int updateCount : ex.getUpdateCounts()) {
                if (updateCount != 1) {
                    exValues.add(batchValues.get(updateIndex));
                }
                updateIndex = updateIndex + 1;
            }
        }
    }

}
