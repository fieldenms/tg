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

import com.google.common.io.Files;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;

/**
 * This is an abstraction that capture the logic for the initial test case related db creation and its re-creation from a generated script for all individual tests in the same test case.
 * <p>
 * It is intended that each individual test case would have a static reference to an instance of this class, which would ensure that the same database is reused for all tests in the same test case to avoid computationally expensive recreation of a database.
 * 
 * @author TG Team
 *
 */
public class DbCreator<T extends AbstractDomainDrivenTestCase> {
    transient private final Logger logger = Logger.getLogger(this.getClass());

    private final List<String> dataScript = new ArrayList<>();
    private final List<String> truncateScript = new ArrayList<>();

    private final Collection<PersistedEntityMetadata<?>> entityMetadatas;
    public final IDomainDrivenTestCaseConfiguration config;
    private final Properties defaultDbProps; // mainly used for db creation and population at the time of loading the test case classes
    
    private final Class<T> testCaseType;

    public DbCreator(final Class<T> testCaseType) {
        this.testCaseType = testCaseType;
        defaultDbProps = mkDbProps();
        config = createConfig(testCaseType, mkDbProps());
        entityMetadatas = config.getDomainMetadata().getPersistedEntityMetadatas();
    }

    private static final String baseDir = "./src/test/resources/db";

    private IDomainDrivenTestCaseConfiguration createConfig(final Class<T> testCaseType, final Properties hbc) {
        try {

            final Properties testProps = new Properties();
            final FileInputStream in = new FileInputStream("src/test/resources/test.properties");
            testProps.load(in);
            in.close();

            hbc.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            hbc.setProperty("hibernate.show_sql", "false");
            hbc.setProperty("hibernate.format_sql", "true");
            hbc.setProperty("hibernate.hbm2ddl.auto", "create");

            final String configClassName = testProps.getProperty("config-domain");
            final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
            final Constructor<IDomainDrivenTestCaseConfiguration> constructor = type.getConstructor(Properties.class);
            return constructor.newInstance(hbc);
        } catch (final Exception e) {
            throw new IllegalStateException(format("Could not create a configuration for test case [%s]", testCaseType.getSimpleName()), e);
        }
    }
    
    /**
     * Creates db connectivity properties.
     * The database name is generated based on the test class name and the current thread id.
     * 
     * @return
     */
    public final Properties mkDbProps() {
        final Properties dbProps = new Properties();
        // TODO Due to incorrect generation of constraints by Hibernate, at this stage simply disable REFERENTIAL_INTEGRITY by rewriting URL
        //      This should be modified once correct db schema generation is implemented
        dbProps.setProperty("hibernate.connection.url", format("jdbc:h2:%s/test_domain_db_for_%s_%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", baseDir, testCaseType.getSimpleName(), Thread.currentThread().getId()));
        dbProps.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        dbProps.setProperty("hibernate.connection.username", "sa");
        dbProps.setProperty("hibernate.connection.password", "");
        return dbProps;
    }

    private final String dataScriptFile() { 
        return format("%s/data-%s.script", baseDir, testCaseType.getSimpleName());
    }
    
    private final String truncateScriptFile() {
        return format("%s/truncate-%s.script", baseDir, testCaseType.getSimpleName());
    }

    public final DbCreator<T> populateOrRestoreData(final AbstractDomainDrivenTestCase testCase, final boolean restoreData) throws Exception {
        return populateOrRestoreData(testCase, restoreData, defaultDbProps);
    }
    
    public final DbCreator<T> populateOrRestoreData(final AbstractDomainDrivenTestCase testCase, final boolean restoreData, final Properties dbProps) throws Exception {
        if (testCase.useSavedDataPopulationScript() && testCase.saveDataPopulationScriptToFile()) {
            throw new IllegalStateException("useSavedDataPopulationScript() && saveDataPopulationScriptToFile() should not be true at the same time.");
        }

        final Connection conn = createConnection(dbProps);
        Optional<Exception> raisedEx = Optional.empty();

        if (restoreData) {
            // apply data population script
            logger.debug("Executing data population script.");
            exec(dataScript, conn);
        } else {
            try {
                if (testCase.useSavedDataPopulationScript()) {
                    restoreDataFromFile(conn);
                }
                // need to call populateDomain, which might have some initialization even if the actual data saving does not need to occur
                testCase.populateDomain();
            } catch (final Exception ex) {
                raisedEx = Optional.of(ex);
                ex.printStackTrace();
            }

            // record data population statements
            if (!testCase.useSavedDataPopulationScript()) {
                recordDataPopulationScript(testCase.saveDataPopulationScriptToFile(), conn);
            }
        }

        conn.close();

        if (raisedEx.isPresent()) {
            throw new IllegalStateException("Population of the test data has failed.", raisedEx.get());
        }

        return this;
    }
    
    private void restoreDataFromFile(final Connection conn) throws Exception {
        dataScript.clear();
        final File dataPopulationScriptFile = new File(dataScriptFile());
        if (!dataPopulationScriptFile.exists()) {
            throw new IllegalStateException(format("File %s with data population script is missing.", dataScriptFile()));
        }
        dataScript.addAll(Files.readLines(dataPopulationScriptFile, StandardCharsets.UTF_8));

        truncateScript.clear();
        final File truncateTablesScriptFile = new File(truncateScriptFile());
        if (!truncateTablesScriptFile.exists()) {
            throw new IllegalStateException(format("File %s with table truncation script is missing.", truncateTablesScriptFile));
        }
        truncateScript.addAll(Files.readLines(truncateTablesScriptFile, StandardCharsets.UTF_8));

        exec(dataScript, conn);
    }

    private void recordDataPopulationScript(final boolean shouldSaveDataPopulationScriptToFile, final Connection conn) throws Exception {
        final Statement st = conn.createStatement();
        final ResultSet set = st.executeQuery("SCRIPT");
        while (set.next()) {
            final String result = set.getString(1).trim();
            final String upperCasedResult = result.toUpperCase();
            if (!upperCasedResult.startsWith("INSERT INTO PUBLIC.UNIQUE_ID")
                    && (upperCasedResult.startsWith("INSERT") || upperCasedResult.startsWith("UPDATE") || upperCasedResult.startsWith("DELETE"))) {
                // resultant script should NOT be UPPERCASED in order not to upperCase for e.g. values,
                // that was perhaps lover cased while populateDomain() invocation was performed
                dataScript.add(result.replace("\n", " ").replace("\r", " "));
            }
        }
        set.close();
        st.close();

        // create truncate statements
        for (final PersistedEntityMetadata<?> entry : entityMetadatas) {
            truncateScript.add(format("TRUNCATE TABLE %s;", entry.getTable()));
        }

        if (shouldSaveDataPopulationScriptToFile) {
            // flush data population script to file for later use
            try (PrintWriter out = new PrintWriter(dataScriptFile(), StandardCharsets.UTF_8.name())) {
                final StringBuilder builder = new StringBuilder();
                for (final Iterator<String> iter = dataScript.iterator(); iter.hasNext();) {
                    final String line = iter.next();
                    builder.append(line);
                    if (iter.hasNext()) {
                        builder.append("\n");
                    }
                }
                out.print(builder.toString());
            }

            // flush table truncation script to file for later use
            try (PrintWriter out = new PrintWriter(truncateScriptFile(), StandardCharsets.UTF_8.name())) {
                final StringBuilder builder = new StringBuilder();
                for (final Iterator<String> iter = truncateScript.iterator(); iter.hasNext();) {
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

    private void exec(final List<String> statements, final Connection conn) throws SQLException {
        final Statement st = conn.createStatement();
        for (final String stmt : statements) {
            st.execute(stmt);
        }
        st.close();
    }

    public final void clearData() throws Exception {
        exec(truncateScript, createConnection(defaultDbProps));
        logger.debug("Executing tables truncation script.");
    }
    
    public final void clearData(final Properties dbProps) throws Exception {
        exec(truncateScript, createConnection(dbProps));
        logger.debug("Executing tables truncation script.");
    }

    private Connection createConnection(final Properties props) {
        final String url = props.getProperty("hibernate.connection.url");
        final String jdbcDriver = props.getProperty("hibernate.connection.driver_class");
        final String user = props.getProperty("hibernate.connection.username");
        final String passwd = props.getProperty("hibernate.connection.password");

        try {
            Class.forName(jdbcDriver);
            return DriverManager.getConnection(url, user, passwd);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
