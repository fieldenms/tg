package ua.com.fielden.platform.test;

import com.google.common.io.Files;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.ddl.IDdlGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;
import ua.com.fielden.platform.utils.DbUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.DbVersion.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.test.AbstractDomainDrivenTestCase.DEFAULT_ID_SEED;
import static ua.com.fielden.platform.test.AbstractDomainDrivenTestCase.LOAD_DATA_SCRIPT_FROM_FILE;
import static ua.com.fielden.platform.utils.DbUtils.batchExecSql;

/// Abstracts the logic for creating the initial test-case database
/// and re-creating it from a generated script for all tests in a test class.
///
/// Each test class is associated with its own `DbCreator` instance.
/// This type is the base for all database creator implementations,
/// and concrete subclasses provide the actual database interaction details.
///
/// ##### Definition of test data
/// **Test data** is the data specific to a given test class and is the only data present before each test in that class is executed.
/// By this definition, each test in the class has data isolation and cannot affect data used by other tests.
///
/// Each test class can define a custom data population procedure by overriding [AbstractDomainDrivenTestCase#populateDomain()].
/// Additionally, test data can optionally be loaded from a file if [AbstractDomainDrivenTestCase#useSavedDataPopulationScript()] returns `true`.
/// The combined data from these two sources is referred to as **test data**.
///
/// ##### How the DB creator works
/// A `DbCreator` coordinates with the test class as follows:
///
/// * Before each test runs, the database contains only the test data for that test class.
/// * After each individual test finishes, the database is cleared by deleting all data.
///
/// This lifecycle guarantees data isolation for individual tests within the same test class.
///
/// The data population procedure in the test class is executed only once, before the first test runs.
/// The resulting data is then extracted as SQL `INSERT` statements and stored by the `DbCreator`
/// so it can be reapplied before each subsequent test.
///
/// Additionally, each test class can optionally:
///
/// * **Save** test data to a file.
///   Enabled when  [AbstractDomainDrivenTestCase#saveDataPopulationScriptToFile()] returns `true`.
///
/// * **Load** test data from a file.
///   Enabled when [AbstractDomainDrivenTestCase#useSavedDataPopulationScript()] returns `true`.
///   When enabled, test data is first loaded from the file, and then the custom data population procedure is run.
///   It is therefore common for the population procedure to first check whether it needs to run, for example by verifying whether the expected data already exists.
///   For this option to be effective, test data must have been previously saved to a file.
///
public abstract class DbCreator {

    public static final String baseDir = "./src/test/resources/db";
    public static final String ddlScriptFileName = format("%s/create-db-ddl.script", DbCreator.baseDir);

    public static final int BATCH_SIZE = 1000;

    public static String prePopulationScriptPath(final String name) {
        return format("%s/prePopulate-%s.script", DbCreator.baseDir, name);
    }

    private static boolean isPrePopulationScript(final Path path) {
        final var filename = path.getFileName().toString();
        return filename.startsWith("prePopulate-") && filename.endsWith(".script");
    }

    private static boolean PRE_POPULATED = false;

    /// The ID seed that gets assigned based on pre-populated data.
    /// It is guaranteed to be greater than any entity ID in all of pre-populated data.
    /// It is relevant only for tests in Cached Mode.
    ///
    /// This field is guaranteed to be non-null when [#PRE_POPULATED] is `true`.
    ///
    private static Long PRE_POPULATED_ID_SEED = null;

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
            config.getInstance(TransactionalExecution.class).execStrict(conn -> batchExecSql(maybeDdl, conn, BATCH_SIZE));
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
    public final DbCreator populateOrRestoreData(final AbstractDomainDrivenTestCase testCase) {
        runPrePopulation(testCase);

        final var dbUtils = config.getInstance(DbUtils.class);

        if (testCase.useSavedDataPopulationScript() && testCase.saveDataPopulationScriptToFile()) {
            throw new DomainDrivenTestException("useSavedDataPopulationScript() && saveDataPopulationScriptToFile() should not be true at the same time.");
        }

        Optional<Exception> raisedEx = Optional.empty();

        // For the first test in the test class: initial population of test data.
        if (dataScripts.isEmpty()) {
            try {
                if (testCase.useSavedDataPopulationScript()) {
                    config.getInstance(TransactionalExecution.class).execStrict(conn -> restoreDataFromFile(testCaseType, conn));
                    // After populating data from a script, the ID sequence remains unchanged, so we have to restart it ourselves.
                    // This is to prevent ID conflicts with populateDomain() which may save new entities.
                    testCase.setIdSeed(AbstractDomainDrivenTestCase.ID_HEADROOM + dbUtils.maxEntityId());
                    testCase.resetIdGenerator();
                }
                // Call populateDomain regardless of using a data population script -- populateDomain may contain extra initialisation.
                testCase.populateDomain();
                testCase.setIdSeed(AbstractDomainDrivenTestCase.ID_HEADROOM + dbUtils.maxEntityId());
                // No need to resetIdGenerator here, each test class has a @Before method that will do this.
            } catch (final Exception ex) {
                raisedEx = Optional.of(ex);
            }

            // Record data population statements.
            if (!testCase.useSavedDataPopulationScript() && dataScripts.isEmpty()) {
                try {
                    config.getInstance(TransactionalExecution.class).execStrict(conn -> recordDataPopulationScript(testCase, conn));
                } catch (final Exception ex) {
                    throw new DomainDrivenTestException("Could not record data population script.", ex);
                }
            }
        }
        // After the first test in the test class: repopulation of test data.
        else {
            // Apply the data population script.
            logger.debug("Executing data population script.");
            config.getInstance(TransactionalExecution.class).execStrict(conn -> batchExecSql(new ArrayList<>(dataScripts), conn, BATCH_SIZE));
        }

        if (raisedEx.isPresent()) {
            raisedEx.ifPresent(ex -> logger.fatal(ex.getMessage(), ex));
            throw new DomainDrivenTestException("Population of the test data has failed.", raisedEx.get());
        }

        return this;
    }

    private void runPrePopulation(final AbstractDomainDrivenTestCase testCase) {
        final var loadDataScriptFromFile = Boolean.getBoolean(LOAD_DATA_SCRIPT_FROM_FILE);
        final var dbUtils = config.getInstance(DbUtils.class);
        final var dbVersionProvider = config.getInstance(IDbVersionProvider.class);
        final var testCaseName = PropertyTypeDeterminator.stripIfNeeded(testCase.getClass()).getSimpleName();

        // Cached Mode: pre-populate or load from file.
        if (!testCase.skipCaching()) {
            logger.info(() -> "%s: Cached Mode is active.".formatted(testCaseName));
            // Pre-population occurs only once per JVM (controlled by PRE_POPULATED).
            if (!PRE_POPULATED) {
                if (!loadDataScriptFromFile) {
                    logger.info(() -> "Creating all pre-population scripts.");

                    // Delete all existing pre-population scripts.
                    if (java.nio.file.Files.exists(Path.of(baseDir))) {
                        logger.info(() -> "Deleting existing pre-population scripts.");
                        try (final Stream<Path> paths = java.nio.file.Files.list(Path.of(baseDir))) {
                            final var delCount = paths.filter(DbCreator::isPrePopulationScript)
                                    .filter(p -> {
                                        try {
                                            return java.nio.file.Files.deleteIfExists(p);
                                        } catch (final IOException ex) {
                                            logger.warn(() -> "Could not delete pre-population script [%s]. This may affect test results.".formatted(p), ex);
                                            return false;
                                        }
                                    }).count();
                            logger.info(() -> "Deleted %s pre-population scripts.".formatted(delCount));
                        } catch (final IOException ex) {
                            logger.warn(() -> "Could not list existing pre-population scripts. This may affect test results.", ex);
                        }
                    }

                    // Delete the ID seed script.
                    try {
                        if (java.nio.file.Files.deleteIfExists(Path.of(idSequenceScriptPath()))) {
                            logger.info(() -> "Deleted [%s].".formatted(idSequenceScriptPath()));
                        }
                    } catch (final IOException ex) {
                        logger.warn(() -> "Could not delete [%s].".formatted(idSequenceScriptPath()), ex);
                    }

                    // let's use non-strict mode for scripting
                    try {
                        AbstractEntity.useNonStrictModelVerification();
                        testCase.prePopulateDomain();
                    } finally {
                        // reset model verification mode to strict after scripting
                        AbstractEntity.useStrictModelVerification();
                    }

                    PRE_POPULATED_ID_SEED = AbstractDomainDrivenTestCase.ID_HEADROOM + dbUtils.maxEntityId();
                    saveScriptToFile(List.of(dbUtils.sqlRestartSequence(dbVersionProvider.dbVersion(), ID_SEQUENCE_NAME, PRE_POPULATED_ID_SEED)),
                                     idSequenceScriptPath());
                    logger.info(() -> "%s: Created %s with ID=%s.".formatted(testCaseName, idSequenceScriptPath(), PRE_POPULATED_ID_SEED));

                    logger.info(() -> "%s: Completed creating all pre-population scripts. Clearing the DB.".formatted(testCaseName));

                    // After pre-population clear the DB for the upcoming test case.
                    try {
                        testCase.afterPrePopulation();
                        config.getInstance(TransactionalExecution.class).execStrict(conn -> {
                            final List<String> script = genTruncStmt(persistentEntitiesMetadata(), conn);
                            batchExecSql(script, conn, DbCreator.BATCH_SIZE);
                        });
                    } catch (final Exception ex) {
                        final String msg = "%s: Failed to clear the DB after pre-population.".formatted(testCaseName);
                        logger.fatal(msg, ex);
                        throw new DomainDrivenTestException(msg, ex);
                    }
                }
                else {
                    // loadDataScriptFromFile = true means that a prior Cached Mode test run performed pre-population.
                    // Load the seed ID from a script created by that test run.

                    logger.info(() -> format("%s: Skipping pre-population, [%s = %s]. Loading the seed ID.", testCaseName, LOAD_DATA_SCRIPT_FROM_FILE, loadDataScriptFromFile));

                    final var idSequenceScript = new File(idSequenceScriptPath());
                    if (idSequenceScript.exists()) {
                        try {
                            final var lines = Files.readLines(idSequenceScript, StandardCharsets.UTF_8);
                            config.getInstance(TransactionalExecution.class).exec(conn -> batchExecSql(lines, conn, 1));
                        } catch (final Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        PRE_POPULATED_ID_SEED = config.getInstance(TransactionalExecution.class).execWithSession($ -> DbUtils.nextIdValue(ID_SEQUENCE_NAME, $.getSession()));
                    }
                    else {
                        logger.warn(() -> format("%s does not exist, but [%s = %s]."
                                                 + " This may result in entity ID conflicts during test data population."
                                                 + " It is recommended to regenerate all scripts by running all tests with [%s = false].",
                                                 idSequenceScriptPath(), LOAD_DATA_SCRIPT_FROM_FILE, loadDataScriptFromFile, LOAD_DATA_SCRIPT_FROM_FILE));
                        PRE_POPULATED_ID_SEED = DEFAULT_ID_SEED;
                    }
                }
                PRE_POPULATED = true;
            }

            // PRE_POPULATED_ID_SEED should not be null at this point, but let's keep this condition just in case.
            if (PRE_POPULATED_ID_SEED != null && testCase.getIdSeed() == null) {
                testCase.setIdSeed(PRE_POPULATED_ID_SEED);
            }
        }
        else {
            logger.info(() -> "%s: Uncached Mode is active.".formatted(testCaseName));
        }
    }

    /// Executes the script that truncates database tables.
    /// Should be invoked after each unit test.
    ///
    public final void clearData() {
        try {
            logger.debug("Executing tables truncation script.");
            config.getInstance(TransactionalExecution.class).execStrict(conn -> batchExecSql(truncateScripts, conn, BATCH_SIZE));
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

    /// Returns a relative path to the ID sequence script.
    /// This is an SQL script that restarts the entity ID sequence with a value that is greater than any entity ID used in `populate*` scripts.
    ///
    private static String idSequenceScriptPath() {
        return "%s/id-sequence.script".formatted(baseDir);
    }

}
