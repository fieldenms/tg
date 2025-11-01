package ua.com.fielden.platform.test;

import com.google.common.io.Files;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.ddl.IDdlGenerator;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.DbUtils.batchExecSql;

/// An abstraction that encapsulates the logic for creating the initial test case database,
/// and for re-creating it from a generated script across all individual tests within the same test case.
///
public abstract class DbCreator {
    public static final String baseDir = "./src/test/resources/db";
    public static final String ddlScriptFileName = format("%s/create-db-ddl.script", DbCreator.baseDir);

    private static final int BATCH_SIZE = 1000;

    public final IDomainDrivenTestCaseConfiguration config;

    protected final Logger logger = getLogger(getClass());

    private final Class<? extends AbstractDomainDrivenTestCase> testCaseType;
    private final Collection<EntityMetadata.Persistent> persistentEntitiesMetadata;

    private final Set<String> dataScripts = new LinkedHashSet<>();
    private final List<String> truncateScripts = new ArrayList<>();

    public DbCreator(
            final Class<? extends AbstractDomainDrivenTestCase> testCaseType, 
            final Properties defaultDbProps,
            final IDomainDrivenTestCaseConfiguration config,
            final List<String> maybeDdl,
            final boolean execDdslScripts) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        this.config = config;

        this.testCaseType = testCaseType;
        this.persistentEntitiesMetadata = config.getInstance(IDomainMetadataUtils.class)
                .registeredEntities()
                .map(EntityMetadata::asPersistent)
                .flatMap(Optional::stream)
                .collect(toImmutableList());

        if (maybeDdl.isEmpty()) {
            logger.debug("GENERATING DDL...");
            // let's create the database...
            final Class<?> dialectType = Class.forName(defaultDbProps.getProperty("hibernate.dialect"));
            final Dialect dialect = (Dialect) dialectType.newInstance();
        
            maybeDdl.addAll(genDdl(config.getInstance(IDdlGenerator.class), dialect));
        }
        
        if (execDdslScripts) {
            // recreate DB structures
            logger.debug("CREATING DB SCHEMA...");
            config.getInstance(TransactionalExecution.class).exec(conn -> batchExecSql(maybeDdl, conn, BATCH_SIZE));
            logger.debug(" DONE!");
        }
    }

    /// Identifies the database version that is used for testing.
    ///
    public abstract DbVersion dbVersion();

    /// Override to implement RDBMS-specific DDL script generation.
    ///
    protected abstract List<String> genDdl(final IDdlGenerator ddlGenerator, final Dialect dialect);

    public Collection<EntityMetadata.Persistent> persistentEntitiesMetadata() {
        return persistentEntitiesMetadata;
    }

    /// Executes the test data population logic.
    /// Should be invoked before each unit test.
    ///
    public final DbCreator populateOrRestoreData(final AbstractDomainDrivenTestCase testCase) throws SQLException {
        if (testCase.useSavedDataPopulationScript() && testCase.saveDataPopulationScriptToFile()) {
            throw new DomainDrivenTestException("useSavedDataPopulationScript() && saveDataPopulationScriptToFile() should not be true at the same time.");
        }

        Optional<Exception> raisedEx = Optional.empty();

        if (!dataScripts.isEmpty()) {
            // Apply the data population script.
            logger.debug("Executing data population script.");
            config.getInstance(TransactionalExecution.class).exec(conn -> batchExecSql(new ArrayList<>(dataScripts), conn, BATCH_SIZE));
        } else {
            try {
                if (testCase.useSavedDataPopulationScript()) {
                    config.getInstance(TransactionalExecution.class).exec(conn -> restoreDataFromFile(testCaseType, conn));
                }
                // Need to call populateDomain, which might have some initialization even if the actual data saving does not need to occur.
                testCase.populateDomain();
            } catch (final Exception ex) {
                raisedEx = Optional.of(ex);
            }

            // Record data population statements.
            if (!testCase.useSavedDataPopulationScript() && dataScripts.isEmpty()) {
                try {
                    config.getInstance(TransactionalExecution.class).exec(conn -> recordDataPopulationScript(testCase, conn));
                } catch (final Exception ex) {
                    throw new DomainDrivenTestException("Could not record data population script.", ex);
                }
            }
        }

        if (raisedEx.isPresent()) {
            raisedEx.ifPresent(ex -> logger.fatal(ex.getMessage(), ex));
            throw new DomainDrivenTestException("Population of the test data has failed.", raisedEx.get());
        }

        return this;
    }

    /// Executes the script that truncates database tables.
    /// Should be invoked after each unit test.
    ///
    public final void clearData() {
        try {
            logger.debug("Executing tables truncation script.");
            config.getInstance(TransactionalExecution.class).exec(conn -> batchExecSql(truncateScripts, conn, BATCH_SIZE));
        } catch (final Exception ex) {
            throw new DomainDrivenTestException("Could not clear data.", ex);
        }
    }
    
    /// A helper method to restore data population and truncation scripts from files.
    ///
    private void restoreDataFromFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final Connection conn) {
        try {
            dataScripts.clear();
            final File dataPopulationScriptFile = new File(dataScriptFile(testCaseType));
            if (!dataPopulationScriptFile.exists()) {
                throw new DomainDrivenTestException(format("File %s with data population script is missing.", dataScriptFile(testCaseType)));
            }
            dataScripts.addAll(Files.readLines(dataPopulationScriptFile, StandardCharsets.UTF_8));

            truncateScripts.clear();
            final File truncateTablesScriptFile = new File(truncateScriptFile(testCaseType));
            if (!truncateTablesScriptFile.exists()) {
                throw new DomainDrivenTestException(format("File %s with table truncation script is missing.", truncateTablesScriptFile));
            }
            truncateScripts.addAll(Files.readLines(truncateTablesScriptFile, StandardCharsets.UTF_8));

            batchExecSql(new ArrayList<>(dataScripts), conn, BATCH_SIZE);
        } catch (final IOException ex) {
            throw new DomainDrivenTestException("Could not restore data population and truncation scripts from files.", ex);
        }
    }

    /// Helper method for generating data population and truncation scripts, with the option to save them to files if specified.
    ///
    private void recordDataPopulationScript(final AbstractDomainDrivenTestCase testCase, final Connection conn) {
        try {
            dataScripts.clear();
            dataScripts.addAll(genInsertStmt(persistentEntitiesMetadata, conn));

            truncateScripts.clear();
            truncateScripts.addAll(genTruncStmt(persistentEntitiesMetadata, conn));

            if (testCase.saveDataPopulationScriptToFile()) {
                // flush data population script to file for later use
                saveScriptToFile(new ArrayList<>(dataScripts), dataScriptFile(testCaseType));

                // flush table truncation script to file for later use
                saveScriptToFile(truncateScripts, truncateScriptFile(testCaseType));
            }
        } catch (final Exception ex) {
            throw new DomainDrivenTestException("Could not generate data population script.", ex);
        }
    }

    /// Implement this method to produce SQL statements that delete records from tables corresponding to domain entities.
    ///
    public abstract List<String> genTruncStmt(final Collection<EntityMetadata.Persistent> entityMetadata, final Connection conn);

    /**
     * Implement to generate SQL statements for inserting records that correspond to test domain data that is present currently in the database with the specified connection.
     */
    public abstract List<String> genInsertStmt(final Collection<EntityMetadata.Persistent> entityMetadata, final Connection conn);
    
    protected String dataScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) {
        return format("%s/data-%s.script", baseDir, testCaseType.getSimpleName());
    }
    
    protected String truncateScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) {
        return format("%s/truncate-%s.script", baseDir, testCaseType.getSimpleName());
    }

    /// Helper function to load scripts from a file.
    /// Returns an empty list if the file does not exist, avoiding errors or exceptions.
    ///
    public static List<String> loadScriptFromFile(final String scriptFileName) throws IOException {
        final File dataPopulationScriptFile = new File(scriptFileName);
        if (!dataPopulationScriptFile.exists()) {
            return new ArrayList<>();
        }
        return Files.readLines(dataPopulationScriptFile, StandardCharsets.UTF_8);
    }

    /// Helper function to save scripts to files.
    ///
    public static void saveScriptToFile(final List<String> scripts, final String fileName) {
        try {
            Files.createParentDirs(new File(fileName));
        } catch (final IOException ex) {
            throw new DomainDrivenTestException("Failed to create parent directories for [%s]".formatted(fileName), ex);
        }

        final var path = Paths.get(fileName);
        try {
            java.nio.file.Files.write(path, scripts);
        } catch (final IOException ex) {
            throw new DomainDrivenTestException("Could not save [%d] scripts to file [%s].".formatted(scripts.size(), fileName), ex);
        }
    }

}
