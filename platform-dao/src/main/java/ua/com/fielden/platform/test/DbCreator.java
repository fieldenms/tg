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

import com.google.common.io.Files;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;
import ua.com.fielden.platform.utils.DbUtils;

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
    
    private Connection conn; // allocated during the first data population attempt and then reused for all tests in the same test case
    
    private final Class<? extends AbstractDomainDrivenTestCase> testCaseType;

    // the following two properties must be static to perform their allocation only once due to its memory and CPU intencity
    private static Collection<PersistedEntityMetadata<?>> entityMetadatas;
    public static IDomainDrivenTestCaseConfiguration config;
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
        
        if (maybeDdl.isEmpty()) {
            // let's create the database...
            final Class<?> dialectType = Class.forName(defaultDbProps.getProperty("hibernate.dialect"));
            final Dialect dialect = (Dialect) dialectType.newInstance();
        
            maybeDdl.addAll(genDdl(dialect));
            
            // recreate DB structures
            System.out.print("CREATING DB SCHEMA...");
            DbUtils.execSql(maybeDdl, config.getInstance(HibernateUtil.class).getSessionFactory().getCurrentSession());
            System.out.println(" DONE!");
        }
    }

    /**
     * Override to implement RDBMS specific DDL script generation.
     * 
     * @param dialect
     * @return
     */
    protected abstract List<String> genDdl(final Dialect dialect);
    
    /**
     * A helper function to instantiate test case configuration as specified in <code>src/test/resources/test.properties</code>, property <code>config-domain</code>.
     * 
     * @param props
     * @return
     */
    private static IDomainDrivenTestCaseConfiguration createConfig(final Properties props) {
        try {

            final Properties testProps = new Properties();
            final FileInputStream in = new FileInputStream("src/test/resources/test.properties");
            testProps.load(in);
            in.close();

            final String configClassName = testProps.getProperty("config-domain");
            final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
            final Constructor<IDomainDrivenTestCaseConfiguration> constructor = type.getConstructor(Properties.class);
            return constructor.newInstance(props);
        } catch (final Exception e) {
            throw new DomainDriventTestException("Could not create test configuration.", e);
        }
    }
    
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

    protected String dataScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) { 
        return format("%s/data-%s.script", baseDir, testCaseType.getSimpleName());
    }
    
    protected String truncateScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) {
        return format("%s/truncate-%s.script", baseDir, testCaseType.getSimpleName());
    }

    public final DbCreator populateOrRestoreData(final AbstractDomainDrivenTestCase testCase) throws Exception {
        if (testCase.useSavedDataPopulationScript() && testCase.saveDataPopulationScriptToFile()) {
            throw new IllegalStateException("useSavedDataPopulationScript() && saveDataPopulationScriptToFile() should not be true at the same time.");
        }

        // this is a single place there a new DB connection is established
        final Connection conn = currentConnection(defaultDbProps);
        
        Optional<Exception> raisedEx = Optional.empty();

        if (!dataScripts.isEmpty()) {
            // apply data population script
            logger.debug("Executing data population script.");
            exec(dataScripts, conn);
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
                ex.printStackTrace();
            }

            // record data population statements
            if (!testCase.useSavedDataPopulationScript()) {
                try {
                    recordDataPopulationScript(testCase, conn);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    throw new DomainDriventTestException("Could not record data population script.");
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
    
    private void restoreDataFromFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final Connection conn) throws Exception {
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

        exec(dataScripts, conn);
    }

    private void recordDataPopulationScript(final AbstractDomainDrivenTestCase testCase, final Connection conn) throws Exception {
        if (dataScripts.isEmpty()) {
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
     * 
     * @param statements
     * @param conn
     * @throws SQLException
     */
    private static void exec(final List<String> statements, final Connection conn) throws SQLException {
        try (final Statement st = conn.createStatement()) {
            for (final String stmt : statements) {
                st.addBatch(stmt);
            }
            st.executeBatch();
        }
    }

    public final void clearData() {
        if (conn == null) {
            throw new DomainDriventTestException("There is no db connection. Please ensure data population is invoked first, which would establish a new db connetion.");
        }

        try {
            exec(truncateScripts, conn);
            logger.debug("Executing tables truncation script.");
            conn.commit();
        } catch (final Exception ex) {
            throw new DomainDriventTestException("Could not clear data.", ex);
        }
    }

    /**
     * Return the current DB connection, or establishes a new one if there is no current connection present.
     * 
     * @param props
     * @return
     */
    private Connection currentConnection(final Properties props) {
        if (conn != null) {
            return conn;
        }
        System.out.println("CREATING DB CONNECTION...");
        final String url = props.getProperty("hibernate.connection.url");
        final String jdbcDriver = props.getProperty("hibernate.connection.driver_class");
        final String user = props.getProperty("hibernate.connection.username");
        final String passwd = props.getProperty("hibernate.connection.password");

        try {
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(url, user, passwd);
            conn.setAutoCommit(false);
            return conn;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Closes the current db connection.
     */
    public void closeConnetion() {
        if (conn != null) {
            System.out.println("CLOSING DB CONNECTION...");
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
