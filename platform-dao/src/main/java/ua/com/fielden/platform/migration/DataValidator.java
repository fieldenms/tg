package ua.com.fielden.platform.migration;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.migration.DataValidatorUtils.getKeyUniquenessViolationSql;
import static ua.com.fielden.platform.migration.DataValidatorUtils.produceValidationSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;

public class DataValidator {
	
    private static final String LONG_BREAK = "\n\n\n";

    private static final Logger LOGGER = getLogger(DataMigrator.class);
    
    private final boolean includeDetails;
    private final Connection conn;
    private final List<CompiledRetriever> retrieversJobs;
    private final Map<Class<? extends AbstractEntity<?>>, List<CompiledRetriever>> entityTypeRetrievers;
    
    public DataValidator(final Connection conn, final boolean includeDetails, final List<CompiledRetriever> retrieversJobs) {
    	this.conn = conn;
    	this.includeDetails = includeDetails;
    	this.retrieversJobs = retrieversJobs;
    	this.entityTypeRetrievers = retrieversJobs.stream().filter(r -> !r.retriever.isUpdater()).collect(Collectors.groupingBy(CompiledRetriever::getType)); 
    }
    
    public void performValidations() {
    	checkRetrievalSqlForSyntaxErrors();
    	checkKeyUniqueness();
        checkRequiredness();
    	checkDataIntegrity();
    }
    
    private boolean validateRetrievalSqlForKeyFieldsUniqueness(final Class<? extends AbstractEntity<?>> entityType, final List<CompiledRetriever> entityTypeRetrievers) {
        final var sql = getKeyUniquenessViolationSql(entityType, entityTypeRetrievers);
        boolean result = false;
        try (final var st = conn.createStatement()) {
            try (final var rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    LOGGER.error(format("There are duplicates in data of [%s].\n"
                            + "%s", entityType.getSimpleName(), includeDetails ? sql + LONG_BREAK : ""));
                    result = true;
                }
            }
        } catch (final Exception ex) {
            LOGGER.error(format("Exception while checking key data uniqueness [%s]%s SQL:\n"
                    + "%s", entityType.getSimpleName(), ex, sql));
            result = true;
        }

        return result;
    }

    private boolean checkDataIntegrity() {
    	var stmts = produceValidationSql(retrieversJobs, entityTypeRetrievers);
    	boolean result = false;
        
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
                result = true;
            }
        }
        return result;
    }

    private boolean checkRequiredness() {
        final var stmts = produceValidationSql(retrieversJobs);
        boolean result = false;
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
                result = true;
            }
        }
        return result;
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
        	validateRetrievalSqlForKeyFieldsUniqueness(ret.getKey(), ret.getValue());
        }
    }
}