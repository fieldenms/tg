package ua.com.fielden.platform.devdb_support;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.data.IDomainDrivenData;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.types.Money;

/**
 * This is a base class for implementing development data population in a domain driven manner. Reuses {@link IDomainDrivenTestCaseConfiguration} for configuration of application
 * specific IoC modules.
 *
 * @author TG Team
 *
 */
public abstract class DomainDrivenDataPopulation implements IDomainDrivenData {

    private final List<String> dataScript = new ArrayList<>();
    private final List<String> truncateScript = new ArrayList<>();

    public final IDomainDrivenTestCaseConfiguration config;
    private final Properties props;

    private final ICompanionObjectFinder provider;
    private final EntityFactory factory;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private boolean domainPopulated = false;

    protected DomainDrivenDataPopulation(final IDomainDrivenTestCaseConfiguration config, final Properties props) {
        try {
            this.props = props;
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
        // generateSchemaScript is only supported for H2 - it uses H2-specific query "SCRIPT"
        final boolean generateSchemaScript = config.getDomainMetadata().dbVersion == H2;
        createAndPopulate(generateSchemaScript);
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
                for (final PersistedEntityMetadata<?> entry : config.getDomainMetadata().getPersistedEntityMetadatas()) {
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

    private Connection createConnection() {
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

    @Override
    public final <T> T getInstance(final Class<T> type) {
        return config.getInstance(type);
    }

    @Override
    public final <T extends AbstractEntity<?>> T save(final T instance) {
        final IEntityDao<T> pp = provider.find((Class<T>) instance.getType());
        return pp.save(instance);
    }

    @Override
    public final <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co(final Class<E> type) {
        return (T) provider.find(type);
    }

    @Override
    public final Date date(final String dateTime) {
        return formatter.parseDateTime(dateTime).toDate();
    }

    @Override
    public final DateTime dateTime(final String dateTime) {
        return formatter.parseDateTime(dateTime);
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
    @Override
    public final <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key, final String desc) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        entity.setDesc(desc);
        return entity;
    }

    /**
     * Instantiates a new entity with a non-composite key, the value for which is provided as the second argument.
     *
     * @param entityClass
     * @param key
     * @return
     */
    @Override
    public final <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        return entity;
    }

    /**
     * Instantiates a new entity based on the provided type only, which leads to creation of a completely empty instance without any of entity properties assigned.
     *
     * @param entityClass
     * @return
     */
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass) {
        final IEntityDao<T> co = co(entityClass);
        return co != null ? co.new_() : factory.newEntity(entityClass);
    }

    /**
     * Instantiates a new entity with composite key, where composite key members are assigned based on the provide value. The order of values must match the order specified in key
     * member definitions. An empty list of key values is permitted.
     *
     * @param entityClass
     * @param keys
     * @return
     */
    @Override
    public final <T extends AbstractEntity<DynamicEntityKey>> T new_composite(final Class<T> entityClass, final Object... keys) {
        final T entity = new_(entityClass);
        if (keys.length > 0) {
            // setting composite key fields
            final List<Field> fieldList = Finder.getKeyMembers(entityClass);
            if (fieldList.size() != keys.length) {
                throw new IllegalArgumentException(format("Number of key values is %s but should be %s", keys.length, fieldList.size()));
            }
            for (int index = 0; index < fieldList.size(); index++) {
                final Field keyField = fieldList.get(index);
                final Object keyValue = keys[index];
                entity.set(keyField.getName(), keyValue);
            }
        }
        return entity;
    }
}
