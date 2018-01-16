package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
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
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;

/**
 * This is an abstraction that capture the logic for the initial test case related db creation and its re-creation from a generated script for all individual tests in the same test case.
 * <p>
 * It is intended that each individual test case would have a static reference to an instance of this class, which would ensure that the same database is reused for all tests in the same test case to avoid computationally expensive recreation of a database.
 * 
 * @author TG Team
 *
 */
public abstract class DbCreator {
    private final Logger logger = Logger.getLogger(getClass());

    private final List<String> dataScripts = new ArrayList<>();
    private final List<String> truncateScripts = new ArrayList<>();
    
    public final String dbUri;
    
    private final Connection conn;
    
    private final Class<? extends AbstractDomainDrivenTestCase> testCaseType;

    // the following two properties must be static to perform their allocation only once due to its memory and CPU intencity
    private static Collection<PersistedEntityMetadata<?>> entityMetadatas;
    private static IDomainDrivenTestCaseConfiguration config;
    private static Properties defaultDbProps; // mainly used for db creation and population at the time of loading the test case classes
    
    public static final String baseDir = "./src/test/resources/db";
    
    
    public DbCreator(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final String dbUri, final List<String> maybeDdl) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.dbUri = dbUri;
        if (config == null) {
            defaultDbProps = mkDbProps(dbUri);
            config = createConfig(defaultDbProps);
            entityMetadatas = config.getDomainMetadata().getPersistedEntityMetadatas();
        }
        
        this.testCaseType = testCaseType;

        // this is a single place where a new DB connection is established
        logger.info("CREATING DB CONNECTION...");
        conn = createConnection(defaultDbProps);
        
        if (maybeDdl.isEmpty()) {
            logger.info("GENERATING DDL...");
            // let's create the database...
            final Class<?> dialectType = Class.forName(defaultDbProps.getProperty("hibernate.dialect"));
            final Dialect dialect = (Dialect) dialectType.newInstance();
        
            maybeDdl.addAll(genDdl(config.getDomainMetadata(), dialect));
            
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

    public final <T> T getInstance(final Class<T> type) {
        return config.getInstance(type);
    }

    /**
     * Override to implement RDBMS specific DDL script generation.
     * 
     * @param domainMetaData
     * @param dialect
     * @return
     */
    protected abstract List<String> genDdl(final DomainMetadata domainMetaData, final Dialect dialect);
    
    /**
     * Creates db connectivity properties in terms of Hibernate properties.
     * The value for property <code>hibernate.connection.url</code> should contain <code>%s</code> as a place holder for the database location and name.
     * For example:
     * <ul>
     * <li><code>jdbc:sqlserver:%s;queryTimeout=30</code>, where <code>%s</code> would be replaced with something like <code>//192.168.1.142:1433;database=TEST_DB</code>.
     * <li><code>jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE</code>, where <code>%s</code> would be replaced with something like <code>./src/test/resources/db/TEST_DB</code>.
     * </ul> 
     * @param dbUri -- the database location and name
     * 
     * @return
     */
    protected abstract Properties mkDbProps(final String dbUri);

    /**
     * Executes test data population logic. Should be executed before each unit test. 
     * 
     * @param testCase
     * @return
     * @throws Exception
     */
    public final DbCreator populateOrRestoreData(final AbstractDomainDrivenTestCase testCase) throws Exception {
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
                throw new IllegalStateException(format("File %s with data population script is missing.", dataScriptFile(testCaseType)));
            }
            dataScripts.addAll(Files.readLines(dataPopulationScriptFile, StandardCharsets.UTF_8));

            truncateScripts.clear();
            final File truncateTablesScriptFile = new File(truncateScriptFile(testCaseType));
            if (!truncateTablesScriptFile.exists()) {
                throw new IllegalStateException(format("File %s with table truncation script is missing.", truncateTablesScriptFile));
            }
            truncateScripts.addAll(Files.readLines(truncateTablesScriptFile, StandardCharsets.UTF_8));

            execSql(dataScripts, conn, 100);
        } catch (final Exception ex) {
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
                try (PrintWriter out = new PrintWriter(dataScriptFile(testCaseType), StandardCharsets.UTF_8.name())) {
                    final StringBuilder builder = new StringBuilder();
                    for (final Iterator<String> iter = dataScripts.iterator(); iter.hasNext();) {
                        final String line = iter.next();
                        builder.append(line);
                        if (iter.hasNext()) {
                            builder.append("\n");
                        }
                    }
                    out.print(builder.toString());
                }

                // flush table truncation script to file for later use
                try (PrintWriter out = new PrintWriter(truncateScriptFile(testCaseType), StandardCharsets.UTF_8.name())) {
                    final StringBuilder builder = new StringBuilder();
                    for (final Iterator<String> iter = truncateScripts.iterator(); iter.hasNext();) {
                        final String line = iter.next();
                        builder.append(line);
                        if (iter.hasNext()) {
                            builder.append("\n");
                        }
                    }
                    out.print(builder.toString());
                }
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
    protected abstract List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn);

    /**
     * Implement to generate SQL statements for inserting records that correspond to test domain data that is present currently in the database with the specified connection.
     * 
     * @param entityMetadata
     * @param conn
     * @return
     * @throws SQLException
     */
    protected abstract List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException;
    
    /**
     * A helper function to execute SQL statements.
     * Executes statements in batches of <code>batchSize</code>. 
     * If <code>barchSize</code> is 0 or negative then no batching is used.
     * 
     * @param statements
     * @param conn
     * @param batchSize
     */
    private static void execSql(final List<String> statements, final Connection conn, final int batchSize) {
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
     * A helper function to instantiate test case configuration as specified in <code>src/test/resources/test.properties</code>, property <code>config-domain</code>.
     * 
     * @param props
     * @return
     */
    private static IDomainDrivenTestCaseConfiguration createConfig(final Properties props) {
        try {
            final Properties testProps = new Properties();
            try (final FileInputStream in = new FileInputStream("src/test/resources/test.properties")) {
                testProps.load(in);
            }

            final String configClassName = testProps.getProperty("config-domain");
            final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
            final Constructor<IDomainDrivenTestCaseConfiguration> constructor = type.getConstructor(Properties.class);
            return constructor.newInstance(props);
        } catch (final Exception ex) {
            throw new DomainDriventTestException("Could not create test configuration.", ex);
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

}
