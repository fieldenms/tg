package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
import org.hibernate.dialect.H2Dialect;

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
public final class DbCreator {
    private final Logger logger = Logger.getLogger(DbCreator.class);

    private final List<String> dataScripts = new ArrayList<>();
    private final List<String> truncateScripts = new ArrayList<>();

    private static Collection<PersistedEntityMetadata<?>> entityMetadatas;
    public static IDomainDrivenTestCaseConfiguration config;
    private static Properties defaultDbProps; // mainly used for db creation and population at the time of loading the test case classes
    
    public static final String baseDir = "./src/test/resources/db";
    
    public final String dbName;
    private Connection conn;
    
    private final Class<? extends AbstractDomainDrivenTestCase> testCaseType;
    
    public DbCreator(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final String dbName, final List<String> maybeDdl) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.dbName = dbName;
        if (config == null) {
            defaultDbProps = mkDbProps();
            config = createConfig();
            entityMetadatas = config.getDomainMetadata().getPersistedEntityMetadatas();
        }
        
        this.testCaseType = testCaseType;
        
        final List<String> ddl;
        if (maybeDdl.isEmpty()) {
            // let's create the database...
            final Class<?> dialectType = Class.forName(defaultDbProps.getProperty("hibernate.dialect"));
            final Dialect dialect = (Dialect) dialectType.newInstance();

            //System.out.println("GENERATING");
            final List<String> createDdl = config.getDomainMetadata().generateDatabaseDdl(dialect);
            ddl = dialect instanceof H2Dialect ? 
                    DbUtils.prependDropDdlForH2(createDdl) : 
                    DbUtils.prependDropDdlForSqlServer(createDdl);
                    
            maybeDdl.addAll(ddl);        
        } else {
            ddl = maybeDdl;
        }
        
        // recreate DB structures
        DbUtils.execSql(ddl, config.getInstance(HibernateUtil.class).getSessionFactory().getCurrentSession());
    }

    private IDomainDrivenTestCaseConfiguration createConfig() {
        try {

            final Properties testProps = new Properties();
            final FileInputStream in = new FileInputStream("src/test/resources/test.properties");
            testProps.load(in);
            in.close();

            defaultDbProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            defaultDbProps.setProperty("hibernate.show_sql", "false");
            defaultDbProps.setProperty("hibernate.format_sql", "true");
            //defaultDbProps.setProperty("hibernate.hbm2ddl.auto", "create");

            final String configClassName = testProps.getProperty("config-domain");
            final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
            final Constructor<IDomainDrivenTestCaseConfiguration> constructor = type.getConstructor(Properties.class);
            return constructor.newInstance(defaultDbProps);
        } catch (final Exception e) {
            throw new IllegalStateException(format("Could not create a configuration."), e);
        }
    }
    
    /**
     * Creates db connectivity properties.
     * The database name is generated based on the test class name and the current thread id.
     * 
     * @return
     */
    private final Properties mkDbProps() {
        final Properties dbProps = new Properties();
        // TODO Due to incorrect generation of constraints by Hibernate, at this stage simply disable REFERENTIAL_INTEGRITY by rewriting URL
        //      This should be modified once correct db schema generation is implemented
        dbProps.setProperty("hibernate.connection.url", format("jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", dbName));
        dbProps.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        dbProps.setProperty("hibernate.connection.username", "sa");
        dbProps.setProperty("hibernate.connection.password", "");
        return dbProps;
    }

    private final String dataScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) { 
        return format("%s/data-%s.script", baseDir, testCaseType.getSimpleName());
    }
    
    private final String truncateScriptFile(final Class<? extends AbstractDomainDrivenTestCase> testCaseType) {
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
                recordDataPopulationScript(testCase, conn);
                conn.commit();
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
            final Statement st = conn.createStatement();
            final ResultSet set = st.executeQuery("SCRIPT");
            while (set.next()) {
                final String result = set.getString(1).trim();
                final String upperCasedResult = result.toUpperCase();
                if (!upperCasedResult.startsWith("INSERT INTO PUBLIC.UNIQUE_ID")
                        && (upperCasedResult.startsWith("INSERT") || upperCasedResult.startsWith("UPDATE") || upperCasedResult.startsWith("DELETE"))) {
                    // resultant script should NOT be UPPERCASED in order not to upperCase for e.g. values,
                    // that was perhaps lover cased while populateDomain() invocation was performed
                    dataScripts.add(result.replace("\n", " ").replace("\r", " "));
                }
            }
            set.close();
            st.close();

            // create truncate statements
            if (truncateScripts.isEmpty()) {
                for (final PersistedEntityMetadata<?> entry : entityMetadatas) {
                    truncateScripts.add(format("TRUNCATE TABLE %s;", entry.getTable()));
                }
            }

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

    private void exec(final List<String> statements, final Connection conn) throws SQLException {
        try (final Statement st = conn.createStatement()) {
            for (final String stmt : statements) {
                st.execute(stmt);
            }
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
