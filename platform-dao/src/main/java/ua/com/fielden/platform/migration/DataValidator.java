package ua.com.fielden.platform.migration;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.migration.DataValidatorUtils.produceDataIntegrityValidationSql;
import static ua.com.fielden.platform.migration.DataValidatorUtils.produceKeyUniquenessViolationSql;
import static ua.com.fielden.platform.migration.DataValidatorUtils.produceRequirednessValidationSql;
import static ua.com.fielden.platform.migration.DataValidatorUtils.produceUpdatersKeysDataIntegrityValidationSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import ua.com.fielden.platform.entity.AbstractEntity;

public class DataValidator {

    private static final String LONG_BREAK = "\n\n\n";

    private static final Logger LOGGER = getLogger(DataMigrator.class);

    private final boolean includeDetails;
    private final Connection conn;
    private final List<CompiledRetriever> retrieversJobs;
    private final Map<Class<? extends AbstractEntity<?>>, List<CompiledRetriever>> entityTypeRetrievers;
    private final Map<CompiledRetriever, List<CompiledRetriever>> domainTypeRetrieversByUpdaters;

    public DataValidator(final Connection conn, final boolean includeDetails, final List<CompiledRetriever> retrieversJobs) {
        this.conn = conn;
        this.includeDetails = includeDetails;
        this.retrieversJobs = retrieversJobs;
        this.entityTypeRetrievers = retrieversJobs.stream().filter(r -> !r.retriever.isUpdater()).collect(Collectors.groupingBy(CompiledRetriever::getType));
        this.domainTypeRetrieversByUpdaters = domainTypeRetrieversByUpdater(retrieversJobs);
    }

    public void performValidations() {
        checkRetrievalSqlForSyntaxErrors();
        checkKeyUniqueness();
        checkRequiredness();
        checkDataIntegrity();
        checkDataIntegrityOfUpdatersKeys();
    }

    private void checkDataIntegrity() {
        final var stmts = produceDataIntegrityValidationSql(retrieversJobs);

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

    private void checkDataIntegrityOfUpdatersKeys() {
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

    private void checkRequiredness() {
        final var stmts = produceRequirednessValidationSql(retrieversJobs);
        LOGGER.debug("Checking requiredness ...");
        for (final var sql : stmts) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(sql._3)) {
                rs.next();
                final var count = rs.getInt(1);
                if (count > 0) {
                    LOGGER.error(format("Violated requiredness records count for property [%s] within retriever [%s] is [%s].\n"
                            + "%s", sql._2, sql._1, count, includeDetails ? sql + LONG_BREAK : ""));
                }
            } catch (final SQLException ex) {
                LOGGER.error("Exception while counting records with violated requiredness with SQL:\n" + sql, ex);
            }
        }
    }

    private void checkRetrievalSqlForSyntaxErrors() {
        LOGGER.debug("Checking SQL syntax correctness ... ");
        for (final var rj : retrieversJobs) {
            try (final var st = conn.createStatement(); final var rs = st.executeQuery(rj.legacySql)) {
            } catch (final Exception ex) {
                LOGGER.error("Exception while checking sql syntax for [" + rj.retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + rj.legacySql);
            }
        }
    }

    private void checkKeyUniqueness() {
        LOGGER.debug("Checking key values uniqueness ... ");
        for (final var ret : entityTypeRetrievers.entrySet()) {
            final var sql = produceKeyUniquenessViolationSql(ret.getKey(), ret.getValue());
            try (final var st = conn.createStatement()) {
                try (final var rs = st.executeQuery(sql)) {
                    if (rs.next()) {
                        LOGGER.error(format("There are duplicates in data of [%s].\n"
                                + "%s", ret.getKey().getSimpleName(), includeDetails ? sql + LONG_BREAK : ""));
                    }
                }
            } catch (final Exception ex) {
                LOGGER.error(format("Exception while checking key data uniqueness [%s]%s SQL:\n"
                        + "%s", ret.getKey().getSimpleName(), ex, sql));
            }

        }
    }

    private static Map<CompiledRetriever, List<CompiledRetriever>> domainTypeRetrieversByUpdater(final List<CompiledRetriever> retrieversJobs) {
        final var allRetrieverByType = retrieversJobs.stream().collect(Collectors.groupingBy(CompiledRetriever::getType));

        final var result = new HashMap<CompiledRetriever, List<CompiledRetriever>>();

        for (final Entry<? extends Class<? extends AbstractEntity<?>>, List<CompiledRetriever>> typeAndItsRetrievers : allRetrieverByType.entrySet()) {
            final var currentTypeResult = new HashMap<CompiledRetriever, List<CompiledRetriever>>();

            for (final CompiledRetriever rt : Lists.reverse(typeAndItsRetrievers.getValue())) {
                if (rt.retriever.isUpdater()) {
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
}