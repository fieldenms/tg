package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.dialect.Dialect;

import com.google.common.collect.Iterators;
import com.google.common.io.Files;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;

/**
 * This is an abstraction that capture the logic for the initial test case related db creation and its re-creation from a generated script for all individual tests in the same test case.
 * <p>
 * It is intended that each individual test case may reference an instance of this class to perform optimised (by means of scripting) test data population.
 * 
 * @author TG Team
 *
 */
public abstract class DbCreator {
    public static final String baseDir = "./src/test/resources/db";
    public static final String ddlScriptFileName = format("%s/create-db-ddl.script", DbCreator.baseDir);

    protected final Logger logger = Logger.getLogger(getClass());

    private final Class<? extends AbstractDomainDrivenTestCase> testCaseType;
    public final Connection conn;
    public final Collection<PersistedEntityMetadata<?>> entityMetadatas;

    private final List<String> dataScripts = new ArrayList<>();
    private final List<String> truncateScripts = new ArrayList<>();
    
    
    public DbCreator(
            final Class<? extends AbstractDomainDrivenTestCase> testCaseType, 
            final Properties defaultDbProps,
            final IDomainDrivenTestCaseConfiguration config,
            final List<String> maybeDdl,
            final boolean execDdslScripts) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        this.testCaseType = testCaseType;
        this.entityMetadatas = config.getDomainMetadata().getPersistedEntityMetadatas();

        // this is a single place where a new DB connection is established
        logger.info("CREATING DB CONNECTION...");
        conn = createConnection(defaultDbProps);
        
        if (maybeDdl.isEmpty()) {
            logger.info("GENERATING DDL...");
            // let's create the database...
            final Class<?> dialectType = Class.forName(defaultDbProps.getProperty("hibernate.dialect"));
            final Dialect dialect = (Dialect) dialectType.newInstance();
        
            maybeDdl.addAll(genDdl(config.getDomainMetadata(), dialect));
        }
        
        if (execDdslScripts) {
            // recreate DB structures
            logger.info("CREATING DB SCHEMA...");
            execSql(maybeDdl, conn, 0);
            try {
                conn.commit();
            } catch (final SQLException ex) {
                throw new DomainDriventTestException("Could not commit transaction after creating database schema.", ex);
            }
            logger.info(" DONE!");
        }
    }

    /**
     * Identifies the database version that is used for testing.
     *
     * @return
     */
    public abstract DbVersion dbVersion();

    /**
     * Override to implement RDBMS specific DDL script generation.
     * 
     * @param domainMetaData
     * @param dialect
     * @return
     */
    protected abstract List<String> genDdl(final DomainMetadata domainMetaData, final Dialect dialect);
    
    /**
     * Executes test data population logic. Should be executed before each unit test. 
     * 
     * @param testCase
     * @return
     * @throws Exception
     */
    public final DbCreator populateOrRestoreData(final AbstractDomainDrivenTestCase testCase) throws SQLException {
        if (testCase.useSavedDataPopulationScript() && testCase.saveDataPopulationScriptToFile()) {
            throw new IllegalStateException("useSavedDataPopulationScript() && saveDataPopulationScriptToFile() should not be true at the same time.");
        }

        Optional<Exception> raisedEx = Optional.empty();

        if (!dataScripts.isEmpty()) {
            // apply data population script
            logger.debug("Executing data population script.");
            execSql(dataScripts, conn, 100);
            conn.commit();
        } else {
            try {
                if (testCase.useSavedDataPopulationScript()) {
                    restoreDataFromFile(testCaseType, conn);
                    conn.commit();
                }
                // need to call populateDomain, which might have some initialization even if the actual data saving does not need to occur
                testCase.populateDomain();
            } catch (final Exception ex) {
                raisedEx = Optional.of(ex);
            }

            // record data population statements
            if (!testCase.useSavedDataPopulationScript() && dataScripts.isEmpty()) {
                try {
                    recordDataPopulationScript(testCase, conn);
                } catch (final Exception ex) {
                    throw new DomainDriventTestException("Could not record data population script.", ex);
                } finally {
                    conn.commit();
                }
            }
        }

        if (raisedEx.isPresent()) {
            throw new IllegalStateException("Population of the test data has failed.", raisedEx.get());
        }

        return this;
    }
    
    /**
     * Executes the script for truncating DB tables. Should be invoked after each unit test.
     */
    public final void clearData() {
        try {
            execSql(truncateScripts, conn, 100);
            logger.debug("Executing tables truncation script.");
            conn.commit();
        } catch (final Exception ex) {
            throw new DomainDriventTestException("Could not clear data.", ex);
        }
    }
    
    /**
     * A helper method to restore data population and truncation scripts from files.
     * 
     * @param testCaseType
     * @param conn
     * @throws Exception
     */
    private void restoreDataFromFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final Connection conn) {
        try {
            dataScripts.clear();
            final File dataPopulationScriptFile = new File(dataScriptFile(testCaseType));
            if (!dataPopulationScriptFile.exists()) {
                throw new DomainDriventTestException(format("File %s with data population script is missing.", dataScriptFile(testCaseType)));
            }
            dataScripts.addAll(Files.readLines(dataPopulationScriptFile, StandardCharsets.UTF_8));

            truncateScripts.clear();
            final File truncateTablesScriptFile = new File(truncateScriptFile(testCaseType));
            if (!truncateTablesScriptFile.exists()) {
                throw new DomainDriventTestException(format("File %s with table truncation script is missing.", truncateTablesScriptFile));
            }
            truncateScripts.addAll(Files.readLines(truncateTablesScriptFile, StandardCharsets.UTF_8));

            execSql(dataScripts, conn, 100);
        } catch (final IOException ex) {
            throw new DomainDriventTestException("Could not resotre data population and truncation scripts from files.", ex);
        }
    }
    
    /**
     * A helper method for generating data population and truncation scripts with ability to save them as files if specified.
     * 
     * @param testCase
     * @param conn
     */
    private void recordDataPopulationScript(final AbstractDomainDrivenTestCase testCase, final Connection conn) {
        try {
            dataScripts.clear();
            dataScripts.addAll(genInsertStmt(entityMetadatas, conn));

            truncateScripts.clear();
            truncateScripts.addAll(genTruncStmt(entityMetadatas, conn));

            if (testCase.saveDataPopulationScriptToFile()) {
                // flush data population script to file for later use
                saveScriptToFile(dataScripts, dataScriptFile(testCaseType));

                // flush table truncation script to file for later use
                saveScriptToFile(truncateScripts, truncateScriptFile(testCaseType));
            }
        } catch (final Exception ex) {
            throw new DomainDriventTestException("Could not generate data population script.", ex);
        }
    }

    /**
     * Implement to generate SQL statements for deleting records from tables associated with domain entities.
     * 
     * @param entityMetadata
     * @param conn
     * @return
     */
    public abstract List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn);

    /**
     * Implement to generate SQL statements for inserting records that correspond to test domain data that is present currently in the database with the specified connection.
     * 
     * @param entityMetadata
     * @param conn
     * @return
     * @throws SQLException
     */
    public abstract List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException;
    
    /**
     * A helper function to execute SQL statements.
     * Executes statements in batches of <code>batchSize</code>. 
     * If <code>barchSize</code> is 0 or negative then no batching is used.
     * 
     * @param statements
     * @param conn
     * @param batchSize
     */
    public static void execSql(final List<String> statements, final Connection conn, final int batchSize) {
        try (final Statement st = conn.createStatement()) {
            Iterators.partition(statements.iterator(), batchSize > 0 ? batchSize : 1).forEachRemaining(batch -> {
                try {
                    for (final String stmt : batch) {
                        st.addBatch(stmt);
                    }
                    st.executeBatch();
                } catch (final Exception ex) {
                    throw new DomainDriventTestException("Could not exec batched SQL statements.", ex);
                }
            });
        } catch (final DomainDriventTestException ex) {
            throw ex;
        } catch (final SQLException ex) {
            throw new DomainDriventTestException("Could not create statement.", ex);
        }
    }

    /**
     * Creates a new DB connection based on the provided properties.
     * 
     * @param props
     * @return
     */
    private static Connection createConnection(final Properties props) {
        final String url = props.getProperty("hibernate.connection.url");
        final String jdbcDriver = props.getProperty("hibernate.connection.driver_class");
        final String user = props.getProperty("hibernate.connection.username");
        final String passwd = props.getProperty("hibernate.connection.password");

        try {
            Class.forName(jdbcDriver);
            final Connection conn = DriverManager.getConnection(url, user, passwd);
            conn.setAutoCommit(false);
            return conn;
        } catch (final Exception ex) {
            throw new DomainDriventTestException(format("Could not establish a dabase connection to [%s]", url), ex);
        }
    }

    /**
     * Closes the current db connection.
     */
    public void closeConnetion() {
        logger.info("CLOSING DB CONNECTION...");
        try {
            conn.close();
        } catch (final SQLException ex) {
            logger.fatal("Could not close DB connection.", ex);
        }
    }

    protected String dataScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) { 
        return format("%s/data-%s.script", baseDir, testCaseType.getSimpleName());
    }
    
    protected String truncateScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) {
        return format("%s/truncate-%s.script", baseDir, testCaseType.getSimpleName());
    }

    /**
     * A helper function to load scripts from a file.
     * If the file does not exist then an empty list is returned (i.e. no errors or exceptions).
     * 
     * @param scriptFileName
     * @return
     * @throws IOException
     */
    public static List<String> loadScriptFromFile(final String scriptFileName) throws IOException {
        final File dataPopulationScriptFile = new File(scriptFileName);
        if (!dataPopulationScriptFile.exists()) {
            //logger.warn(format("DDL script file [%s] is missing.", scriptFileName))
            return new ArrayList<>();
        }
        return Files.readLines(dataPopulationScriptFile, StandardCharsets.UTF_8);
    }

    /**
     * A helper function for saving scripts to files.
     * 
     * @param scripts
     * @param fileName
     */
    public static void saveScriptToFile(final List<String> scripts, final String fileName) {
        try (final PrintWriter out = new PrintWriter(fileName, StandardCharsets.UTF_8.name())) {
            final StringBuilder builder = new StringBuilder();
            for (final Iterator<String> iter = scripts.iterator(); iter.hasNext();) {
                final String line = iter.next();
                builder.append(line);
                if (iter.hasNext()) {
                    builder.append("\n");
                }
            }
            out.print(builder.toString());
        } catch (final Exception ex) {
            throw new DomainDriventTestException(format("Could not save [%s] scripts to file [%s].", scripts.size(), fileName));
        }

    }

}
