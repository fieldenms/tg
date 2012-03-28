package ua.com.fielden.platform.test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import ua.com.fielden.platform.dao2.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IDefaultControllerProvider2;
import static java.lang.String.format;


/**
 * This is a base class for all test cases in TG based applications.
 * Each application module should provide file <b>src/test/resources/test.properties</b> with property <code>config-domain</code> assigned an application specific class implementing contract {@link IDomainDrivenTestCaseConfiguration}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainDrivenTestCase {

    private static final List<String> dataScript = new ArrayList<String>();
    private static final List<String> truncateScript = new ArrayList<String>();

    public final static IDomainDrivenTestCaseConfiguration config = createConfig();

    private final IDefaultControllerProvider2 provider = config.getInstance(IDefaultControllerProvider2.class);
    private final EntityFactory factory = config.getEntityFactory();
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static boolean domainPopulated = false;

    private static IDomainDrivenTestCaseConfiguration createConfig() {
	try {
	    final Properties testProps = new Properties();
	    final FileInputStream in = new FileInputStream("src/test/resources/test.properties");
	    testProps.load(in);
	    in.close();

	    // TODO Due to incorrect generation of constraints by Hibernate, at this stage simply disable REFERENTIAL_INTEGRITY by rewriting URL
	    //      This should be modified once correct db schema generation is implemented
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.connection.url", "jdbc:h2:src/test/resources/db/test_domain_db;INIT=SET REFERENTIAL_INTEGRITY FALSE");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.connection.username", "sa");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.connection.password", "");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.show_sql", "false");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.format_sql", "true");
	    IDomainDrivenTestCaseConfiguration.hbc.setProperty("hibernate.hbm2ddl.auto", "create");

	    final Connection conn = createConnection();
	    final Statement st = conn.createStatement();
	    st.execute("DROP ALL OBJECTS");
	    st.close();
	    conn.close();

	    final String configClassName = testProps.getProperty("config-domain");
	    final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
	    return type.newInstance();
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Should be implemented in order to provide domain driven data population.
     */
    protected abstract void populateDomain();

    /**
     * Should return a complete list of domain entity types.
     *
     * @return
     */
    protected abstract List<Class<? extends AbstractEntity<?>>> domainEntityTypes();

    @AfterClass
    public final static void removeDbSchema() {
	domainPopulated = false;
	dataScript.clear();
	truncateScript.clear();
    }

    @Before
    public final void beforeTest() throws Exception {
	final Connection conn = createConnection();

	if (domainPopulated) {
	    // apply data population script
	    exec(dataScript, conn);
	} else {
	    populateDomain();

	    // record data population statements
	    final Statement st = conn.createStatement();
	    final ResultSet set = st.executeQuery("SCRIPT");
	    while (set.next()) {
		final String result = set.getString(1).trim();
		final String upperCasedResult = result.toUpperCase();
		if (!upperCasedResult.startsWith("INSERT INTO PUBLIC.UNIQUE_ID") && (upperCasedResult.startsWith("INSERT") || upperCasedResult.startsWith("UPDATE") || upperCasedResult.startsWith("DELETE"))) {
		    // resultant script should NOT be UPPERCASED in order not to upperCase for e.g. values,
		    // that was perhaps lover cased while populateDomain() invocation was performed
		    dataScript.add(result);
		}
	    }
	    set.close();
	    st.close();

	    // create truncate statements
	    for (final Class<? extends AbstractEntity<?>> entityType : domainEntityTypes()) {
		final String tableName = DomainPersistenceMetadata.getTableClause(entityType);
		truncateScript.add(format("TRUNCATE TABLE %s;", tableName));
	    }

	    domainPopulated = true;
	}

	conn.close();
    }

    private void exec(final List<String> statements, final Connection conn) throws SQLException {
	final Statement st = conn.createStatement();
	for (final String stmt : statements) {
	    st.execute(stmt);
	}
	st.close();
    }

    @After
    public final void afterTest() throws Exception {
	final Connection conn = createConnection();

	System.out.println("TRUNCATE TABLES");
	// TODO need to switch off referential integrity
	exec(truncateScript, conn);
    }

    private static Connection createConnection() {
	final String url = IDomainDrivenTestCaseConfiguration.hbc.getProperty("hibernate.connection.url");
	final String jdbcDriver = IDomainDrivenTestCaseConfiguration.hbc.getProperty("hibernate.connection.driver_class");
	final String user = IDomainDrivenTestCaseConfiguration.hbc.getProperty("hibernate.connection.username");
	final String passwd = IDomainDrivenTestCaseConfiguration.hbc.getProperty("hibernate.connection.password");

	try {
	    Class.forName(jdbcDriver);
	    return DriverManager.getConnection(url, user, passwd);
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    public final <T> T getInstance(final Class<T> type) {
	return config.getInstance(type);
    }

    protected <T extends AbstractEntity<?>> T save(final T instance) {
	final IEntityDao2<T> pp = provider.findController((Class<T>) instance.getType());
	return pp.save(instance);
    }

    protected <T extends IEntityDao2<E>, E extends AbstractEntity<?>> T ao(final Class<E> type) {
	return (T) provider.findController(type);
    }

    public final Date date(final String dateTime) {
	return formatter.parseDateTime(dateTime).toDate();
    }

    public final DateTime dateTime(final String dateTime) {
	return formatter.parseDateTime(dateTime);
    }

    protected <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key, final String desc) {
	return factory.newEntity(entityClass, key, desc);
    }

    protected <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key) {
	return factory.newByKey(entityClass, key);
    }

    protected <T extends AbstractEntity<DynamicEntityKey>> T new_(final Class<T> entityClass, final Object... keys) {
	return keys.length == 0 ? factory.newEntity(entityClass) : factory.newByKey(entityClass, keys);
    }
}
