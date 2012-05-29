package ua.com.fielden.platform.devdb_support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ua.com.fielden.platform.dao.EntityPersistenceMetadata;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IDefaultControllerProvider;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import static java.lang.String.format;


/**
 * This is a base class for implementing development data population in a domain driven manner.
 * Reuses {@link IDomainDrivenTestCaseConfiguration} for configuration of application specific IoC modules.
 *
 * @author TG Team
 *
 */
public abstract class DomainDrivenDataPopulation {

    private final List<String> dataScript = new ArrayList<String>();
    private final List<String> truncateScript = new ArrayList<String>();

    public final IDomainDrivenTestCaseConfiguration config;

    private final IDefaultControllerProvider provider;
    private final EntityFactory factory;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private boolean domainPopulated = false;

    protected DomainDrivenDataPopulation(final IDomainDrivenTestCaseConfiguration config) {
	try {
	    this.config = config;
	    provider = config.getInstance(IDefaultControllerProvider.class);
	    factory = config.getEntityFactory();
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

    public final void removeDbSchema() {
	domainPopulated = false;
	dataScript.clear();
	truncateScript.clear();
    }

    /**
     * The entry point to trigger creation of the database and its population.
     *
     * @throws Exception
     */
    public final void createAndPopulate() throws Exception {
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
		final String result = set.getString(1).toUpperCase().trim();
		if (!result.startsWith("INSERT INTO PUBLIC.UNIQUE_ID") && (result.startsWith("INSERT") || result.startsWith("UPDATE") || result.startsWith("DELETE"))) {
		    dataScript.add(result);
		}
	    }
	    set.close();
	    st.close();

	    // create truncate statements
	    for (final EntityPersistenceMetadata entry : config.getDomainPersistenceMetadata().getHibTypeInfosMap().values()) {
		if (entry.isPersisted()) {
		    truncateScript.add(format("TRUNCATE TABLE %s;", entry.getTable()));
		}
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

    public final void cleanData() throws Exception {
	final Connection conn = createConnection();
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

    public final <T extends AbstractEntity<?>> T save(final T instance) {
	final IEntityDao<T> pp = provider.findController((Class<T>) instance.getType());
	return pp.save(instance);
    }

    public final <T extends IEntityDao<E>, E extends AbstractEntity<?>> T ao(final Class<E> type) {
	return (T) provider.findController(type);
    }

    public final Date date(final String dateTime) {
	return formatter.parseDateTime(dateTime).toDate();
    }

    public final <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key, final String desc) {
	return factory.newEntity(entityClass, key, desc);
    }

    public final <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key) {
	return factory.newByKey(entityClass, key);
    }

    public final <T extends AbstractEntity<DynamicEntityKey>> T new_(final Class<T> entityClass, final Object... keys) {
	return factory.newByKey(entityClass, keys);
    }
}
