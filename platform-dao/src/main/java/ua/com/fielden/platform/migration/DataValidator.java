package ua.com.fielden.platform.migration;

import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

@Singleton
final class DataValidator {

    private static final String LONG_BREAK = "\n\n\n";

    private static final Logger LOGGER = getLogger();

    private final MigrationUtils migrationUtils;
    private final RetrieverSqlProducer sqlProducer;

    @Inject
    DataValidator(final MigrationUtils migrationUtils, final RetrieverSqlProducer sqlProducer) {
        this.migrationUtils = migrationUtils;
        this.sqlProducer = sqlProducer;
    }

    public void performValidations(final Connection conn, final boolean includeDetails, final List<CompiledRetriever> retrievers) {
        checkRetrievalSqlForSyntaxErrors(conn, retrievers);
        checkKeyUniqueness(conn, includeDetails, retrievers);
        checkRequiredness(conn, includeDetails, retrievers);
        checkDataIntegrity(conn, includeDetails, retrievers);
        checkDataIntegrityOfUpdatersKeys(conn, includeDetails, retrievers);
    }

    private void checkDataIntegrity(final Connection conn, final boolean includeDetails, final List<CompiledRetriever> retrievers) {
        final var stmts = produceDataIntegrityValidationSql(retrievers);

        LOGGER.debug("Checking data integrity ...");

        for (final var entry : stmts) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(entry._3)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.error(format("Dead references count for prop [%s] of retriever [%s] is [%s].\n"
                            + "%s", entry._2, entry._1, rs.getInt(1), includeDetails ? entry._3 + LONG_BREAK : ""));
                }
            } catch (final Exception ex) {
                LOGGER.error(format("Exception while counting dead references for prop [%s] of retriever [%s]%s SQL:\n"
                        + "%s", entry._2, entry._1, ex, entry._3));
            }
        }
    }

    private void checkDataIntegrityOfUpdatersKeys(
            final Connection conn,
            final boolean includeDetails,
            final List<CompiledRetriever> retrievers)
    {
        final var domainTypeRetrieversByUpdaters = domainTypeRetrieversByUpdater(retrievers);
        final var stmts = produceUpdatersKeysDataIntegrityValidationSql(domainTypeRetrieversByUpdaters);

        LOGGER.debug("Checking data integrity for updaters keys ...");

        for (final var entry : stmts) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(entry._3)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.error(format("Dead references count for prop [%s] of updater [%s] is [%s].\n"
                            + "%s", entry._2, entry._1, rs.getInt(1), includeDetails ? entry._3 + LONG_BREAK : ""));
                }
            } catch (final Exception ex) {
                LOGGER.error(format("Exception while counting dead references for prop [%s] of updater [%s]%s SQL:\n"
                        + "%s", entry._2, entry._1, ex, entry._3));
            }
        }
    }

    private void checkRequiredness(
            final Connection conn,
            final boolean includeDetails,
            final List<CompiledRetriever> retrievers)
    {
        final var stmts = produceRequirednessValidationSql(retrievers);
        LOGGER.debug(() -> "Checking requiredness ...");
        for (final var sql : stmts) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(sql._3)) {
                rs.next();
                final var count = rs.getInt(1);
                if (count > 0) {
                    LOGGER.error(format("""
                                        Violated requiredness records count for property [%s] within retriever [%s] is [%s].
                                        %s""",
                                        sql._2, sql._1, count, includeDetails ? sql + LONG_BREAK : ""));
                }
            } catch (final SQLException ex) {
                LOGGER.error(() -> "An error occured while counting records with violated requiredness. SQL:\n" + sql, ex);
            }
        }
    }

    private void checkRetrievalSqlForSyntaxErrors(final Connection conn, final List<CompiledRetriever> retrievers) {
        LOGGER.debug("Checking SQL syntax correctness ... ");
        for (final var rj : retrievers) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(rj.legacySql())) {
            } catch (final Exception ex) {
                LOGGER.error("Exception while checking sql syntax for [" + rj.retriever().getClass().getSimpleName() + "]" + ex + " SQL:\n" + rj.legacySql());
            }
        }
    }

    private void checkKeyUniqueness(
            final Connection conn,
            final boolean includeDetails,
            final List<CompiledRetriever> allRetrievers)
    {
        LOGGER.debug("Checking key values uniqueness ... ");
        final var retrieversByType = allRetrievers.stream().filter(r -> !r.isUpdater()).collect(groupingBy(CompiledRetriever::getType));
        retrieversByType.forEach((entityType, retrievers) -> {
            final var sql = produceKeyUniquenessViolationSql(entityType, retrievers);
            try (final var st = conn.createStatement()) {
                try (final var rs = st.executeQuery(sql)) {
                    if (rs.next()) {
                        LOGGER.error(format("""
                                            There are duplicates in data of [%s].
                                            %s""",
                                            entityType.getSimpleName(), includeDetails ? sql + LONG_BREAK : ""));
                    }
                }
            } catch (final Exception ex) {
                LOGGER.error(format("""
                                    An error occured while checking key data uniqueness in [%s]. SQL:
                                    %s""",
                                    entityType.getSimpleName(), sql),
                             ex);
            }

        });
    }

    private static Map<CompiledRetriever, List<CompiledRetriever>> domainTypeRetrieversByUpdater(final List<CompiledRetriever> retrievers) {
        final var retrieversByType = retrievers.stream().collect(groupingBy(CompiledRetriever::getType));

        final var result = new HashMap<CompiledRetriever, List<CompiledRetriever>>();

        for (final Entry<? extends Class<? extends AbstractEntity<?>>, List<CompiledRetriever>> typeAndItsRetrievers : retrieversByType.entrySet()) {
            final var currentTypeResult = new HashMap<CompiledRetriever, List<CompiledRetriever>>();

            for (final CompiledRetriever rt : Lists.reverse(typeAndItsRetrievers.getValue())) {
                if (rt.isUpdater()) {
                    currentTypeResult.put(rt, new ArrayList<>());
                } else {
                    for (final List<CompiledRetriever> updaterDomain : currentTypeResult.values()) {
                        updaterDomain.add(rt);
                    }
                }
            }
            result.putAll(currentTypeResult);
        }
        return result;
    }

    private String produceKeyUniquenessViolationSql(final Class<? extends AbstractEntity<?>> entityType, final List<CompiledRetriever> entityTypeRetrievers) {
        final List<String> keyProps = migrationUtils.keyPaths(entityType);
        final var from = entityTypeRetrievers.stream().map(r -> sqlProducer.getKeyResultsOnlySql(r.retriever(), keyProps)).collect(joining("\nUNION ALL"));
        final var props = keyProps.stream().map(k -> " \"" + k + "\"").collect(joining(", "));
        return "SELECT 1 WHERE EXISTS (\nSELECT *, COUNT(*) FROM (" + from + ") T GROUP BY " + props + " HAVING COUNT(*) > 1\n)";
    }

    private List<T3<String, String, String>> produceRequirednessValidationSql(final List<CompiledRetriever> retrieversJobs) {
        final var result = new ArrayList<T3<String, String, String>>();

        for (final CompiledRetriever retriever : retrieversJobs) {
            final var retrieverSql = sqlProducer.getSqlWithoutOrdering(retriever.retriever());
            for (final PropInfo pi : retriever.getContainers()) {
                final var pmd = retriever.entityMd().props().stream().filter(p -> p.name().equals(pi.propName())).findFirst().get();
                if (pmd.required()) {
                    final List<String> leafProps = isPersistentEntityType(pi.propType()) ? pmd.leafProps() : CollectionUtil.listOf(pmd.name());
                    final var cond = leafProps.stream().map(s -> "R. \"" + s + "\" IS NULL").collect(
                            Collectors.joining(" AND "));
                    final var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond;
                    result.add(T3.t3(retriever.retriever().getClass().getSimpleName(), pi.propName() + ":" + pi.propType().getSimpleName(), sql));
                }
            }
        }

        return result;
    }

    private List<T3<String, String, String>> produceDataIntegrityValidationSql(final List<CompiledRetriever> retrieversJobs) {
        final var result = new ArrayList<T3<String, String, String>>();
        final var entityTypeRetrievers = new HashMap<Class<? extends AbstractEntity<?>>, List<CompiledRetriever>>();

        for (final CompiledRetriever retriever : retrieversJobs) {
            final var retrieverSql = sqlProducer.getSqlWithoutOrdering(retriever.retriever());
            for (final PropInfo pi : retriever.getContainers()) {
                if (isPersistentEntityType(pi.propType())) {
                    final List<String> keyProps = migrationUtils.keyPaths((Class<? extends AbstractEntity<?>>) pi.propType());
                    final List<String> leafProps = retriever.entityMd().props().stream().filter(p -> p.name().equals(pi.propName())).findFirst().get().leafProps();
                    final var domainRets = entityTypeRetrievers.get(pi.propType());
                    final var from = domainRets == null ? null : domainRets.stream().map(r -> sqlProducer.getKeyResultsOnlySql(r.retriever(), keyProps)).collect(joining("\nUNION ALL"));
                    final var cond = "(" + leafProps.stream().map(s -> "R. \"" + s + "\" IS NOT NULL").collect(Collectors.joining(" OR ")) + ")";
                    final var existCond = " AND NOT EXISTS (SELECT * FROM (" + from + ") D WHERE " +
                                          composeCondition(leafProps, keyProps, "R", "D") + ")";
                    final var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond + (from == null ? "" : existCond);
                    result.add(T3.t3(retriever.retriever().getClass().getSimpleName(), pi.propName() + ":" + pi.propType().getSimpleName(), sql));
                }
            }

            if (!retriever.isUpdater()) {
                final var existingOrCreated = entityTypeRetrievers.computeIfAbsent(retriever.retriever().type(), k -> new ArrayList<>());
                existingOrCreated.add(retriever);
            }
        }

        return result;
    }

    private List<T3<String, String, String>> produceUpdatersKeysDataIntegrityValidationSql(final Map<CompiledRetriever, List<CompiledRetriever>> domainTypeRetrieversByUpdaters) {
        final var result = new ArrayList<T3<String, String, String>>();

        for (final Entry<CompiledRetriever, List<CompiledRetriever>> entry : domainTypeRetrieversByUpdaters.entrySet()) {
            final var retrieverSql = sqlProducer.getSqlWithoutOrdering(entry.getKey().retriever());
            final List<String> keyProps = migrationUtils.keyPaths(entry.getKey().getType());
            final var domainRets = entry.getValue();
            final var from = domainRets == null ? null : domainRets.stream().map(r -> sqlProducer.getKeyResultsOnlySql(
                    r.retriever(), keyProps)).collect(joining("\nUNION ALL"));
            final var cond = "(" + keyProps.stream().map(s -> "R. \"" + s + "\" IS NOT NULL").collect(Collectors.joining(" OR ")) + ")";
            final var existCond = " AND NOT EXISTS (SELECT * FROM (" + from + ") D WHERE " +
                                  composeCondition(keyProps, keyProps, "R", "D") + ")";
            final var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond + (from == null ? "" : existCond);
            result.add(T3.t3(entry.getKey().retriever().getClass().getSimpleName(), "key", sql));
        }

        return result;
    }

    private static String composeCondition(final List<String> props, final List<String> keyProps, final String retAlias, final String domainAlias) {
        final List<T2<String, String>> pairs = new ArrayList<>();

        for (int i = 0; i < props.size(); i++) {
            pairs.add(T2.t2(props.get(i), keyProps.get(i)));
        }

        return pairs.stream().map(e -> "(" + retAlias + ".\"" + e._1 + "\" IS NULL AND " + domainAlias + ".\"" + e._2 + "\" IS NULL OR " + retAlias + ".\"" + e._1 + "\" = " + domainAlias + ".\"" + e._2 + "\")").collect(Collectors.joining(" AND "));
    }

}
