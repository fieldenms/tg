package ua.com.fielden.platform.devdb_support;

import static java.lang.String.format;

import java.math.BigDecimal;
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

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.types.Money;

/**
 * This is a base class for implementing development data population in a domain driven manner. Reuses {@link IDomainDrivenTestCaseConfiguration} for configuration of application
 * specific IoC modules.
 *
 * @author TG Team
 *
 */
public abstract class DomainDrivenDataPopulation {

    private final List<String> dataScript = new ArrayList<String>();
    private final List<String> truncateScript = new ArrayList<String>();

    public final IDomainDrivenTestCaseConfiguration config;

    private final ICompanionObjectFinder provider;
    private final EntityFactory factory;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private boolean domainPopulated = false;

    protected DomainDrivenDataPopulation(final IDomainDrivenTestCaseConfiguration config) {
        try {
            this.config = config;
            provider = config.getInstance(ICompanionObjectFinder.class);
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

    public final void createAndPopulate() throws Exception {
        createAndPopulate(true);
    }
    /**
     * The entry point to trigger creation of the database and its population.
     *
     * @throws Exception
     */
    public final void createAndPopulate(final boolean generateSchemaScript) throws Exception {
        final Connection conn = createConnection();

        if (domainPopulated) {
            // apply data population script
            exec(dataScript, conn);
        } else {
            populateDomain();

            // record data population statements
            if (generateSchemaScript) {
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
                for (final PersistedEntityMetadata entry : config.getDomainMetadata().getEntityMetadatas()) {
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
        final IEntityDao<T> pp = provider.find((Class<T>) instance.getType());
        return pp.save(instance);
    }

    public final <T extends IEntityDao<E>, E extends AbstractEntity<?>> T ao(final Class<E> type) {
        return (T) provider.find(type);
    }

    public final Date date(final String dateTime) {
        return formatter.parseDateTime(dateTime).toDate();
    }

    public final BigDecimal decimal(final String value) {
        return new BigDecimal(value);
    }

    public final Money money(final String value) {
        return new Money(value);
    }

    /**
     * Instantiates a new entity with a non-composite key, the value for which is provided as the second argument, and description -- provided as the value for the third argument.
     *
     * @param entityClass
     * @param key
     * @param desc
     * @return
     */
    public final <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key, final String desc) {
        return factory.newEntity(entityClass, key, desc);
    }

    /**
     * Instantiates a new entity with a non-composite key, the value for which is provided as the second argument.
     *
     * @param entityClass
     * @param key
     * @return
     */
    public final <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key) {
        return factory.newByKey(entityClass, key);
    }

    /**
     * Instantiates a new entity based on the provided type only, which leads to creation of a completely empty instance without any of entity properties assigned.
     *
     * @param entityClass
     * @return
     */
    protected <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass) {
        return factory.newEntity(entityClass);
    }

    /**
     * Instantiates a new entity with composite key, where composite key members are assigned based on the provide value. The order of values must match the order specified in key
     * member definitions. An empty list of key values is permitted.
     *
     * @param entityClass
     * @param keys
     * @return
     */
    public final <T extends AbstractEntity<DynamicEntityKey>> T new_composite(final Class<T> entityClass, final Object... keys) {
        return factory.newByKey(entityClass, keys);
    }
}
