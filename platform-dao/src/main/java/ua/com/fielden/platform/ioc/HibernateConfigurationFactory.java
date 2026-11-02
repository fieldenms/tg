package ua.com.fielden.platform.ioc;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.persistence.types.DateTimeType;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/// Hibernate configuration factory.
/// All Hibernate specific properties should be passed as [Properties] values.
///
/// #### DB connection
///
/// - <font color="981515">`hibernate.connection.url`</font> – required;
/// - <font color="981515">`hibernate.connection.driver_class`</font> – required;
/// - <font color="981515">`hibernate.dialect`</font> – required;
/// - <font color="981515">`hibernate.connection.username`</font> – required;
/// - <font color="981515">`hibernate.connection.password`</font> – required;
/// - `hibernate.show_sql` – defaults to `false`;
/// - `hibernate.format_sql` – defaults to `false`;
/// - `hibernate.connection.autocommit`, `hibernate.connection.provider\_disables\_autocommit` and `hibernate.hikari.autoCommit` – set by TG, and cannot be specified (refer to [setAutoCommitSettings]).
///
/// #### DB connection pool providers and their properties
///
/// `hibernate.connection.provider\_class` – HikariCP `org.hibernate.hikaricp.internal.HikariCPConnectionProvider` is used by default;
/// c3p0 `org.hibernate.connection.C3P0ConnectionProvider` is also supported;
///
/// ##### HikariCP configuration properties
///
/// Refer to the official [Gear Configuration](https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby) for more details.
///
/// - `hibernate.hikari.connectionTimeout` – a maximum waiting time in millis for a connection from the pool; defaults to 30000 (30 seconds);
/// - `hibernate.hikari.minimumIdle` -- a minimum number of ideal connections in the pool; defaults to the same value as maximumPoolSize;
/// - `hibernate.hikari.maximumPoolSize` -- a maximum number of actual connections in the pool; defaults to 10 (see [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing));
/// - `hibernate.hikari.idleTimeout` -- a maximum time in millis that a connection is allowed to sit idle in the pool; defaults to 240000 (4 minutes), which is suitable for Azure SQL;
/// - `hibernate.hikari.maxLifetime` -- a maximum lifetime in millis of a connection in the pool; defaults to 270000 (4:30 minutes).
///
/// ##### c3p0 configuration properties
///
/// - `hibernate.c3p0.min_size` -- should accompany the C3P0ConnectionProvider in case it is specified;
/// - `hibernate.c3p0.max_size` -- should accompany the C3P0ConnectionProvider in case it is specified;
/// - `hibernate.c3p0.timeout` -- should accompany the C3P0ConnectionProvider in case it is specified;
/// - `hibernate.c3p0.max_statements` -- should accompany the C3P0ConnectionProvider in case it is specified;
/// - `hibernate.c3p0.acquire_increment` -- should accompany the C3P0ConnectionProvider in case it is specified;
/// - `hibernate.c3p0.idle_test_period` -- should accompany the C3P0ConnectionProvider in case it is specified;
///
public class HibernateConfigurationFactory {

    public static final String
            ERR_AUTOCOMMIT_SETTING_SPECIFIED = "Property [%s] cannot be specified. TG hands out pooled connections with auto-commit disabled, and Hibernate relies on that.";

    static final String
            SHOW_SQL = "hibernate.show_sql",
            FORMAT_SQL = "hibernate.format_sql",
            JDBC_USE_GET_GENERATED_KEYS = "hibernate.jdbc.use_get_generated_keys",
            CONNECTION_PROVIDER_CLASS = "hibernate.connection.provider_class",
            CONNECTION_AUTOCOMMIT = "hibernate.connection.autocommit",
            CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT = "hibernate.connection.provider_disables_autocommit",
            // C3P0 connection pool settings
            C3P0_NUM_HELPER_THREADS = "hibernate.c3p0.numHelperThreads",
            C3P0_MIN_SIZE = "hibernate.c3p0.min_size",
            C3P0_MAX_SIZE = "hibernate.c3p0.max_size",
            C3P0_TIMEOUT = "hibernate.c3p0.timeout",
            C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements",
            C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment",
            C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period",
            // Hikari connection pool settings
            HIKARI_CONNECTION_TIMEOUT = "hibernate.hikari.connectionTimeout",
            HIKARI_MIN_SIZE = "hibernate.hikari.minimumIdle",
            HIKARI_MAX_SIZE = "hibernate.hikari.maximumPoolSize",
            HIKARI_IDLE_TIMEOUT = "hibernate.hikari.idleTimeout",
            HIKARI_MAX_LIFETIME = "hibernate.hikari.maxLifetime",
            HIKARI_AUTO_COMMIT = "hibernate.hikari.autoCommit",
            // More Hibernate settings
            HBM2DDL_AUTO = "hibernate.hbm2ddl.auto",
            CONNECTION_URL = "hibernate.connection.url",
            CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class",
            DIALECT = "hibernate.dialect",
            CONNECTION_USERNAME = "hibernate.connection.username",
            CONNECTION_PASWD = "hibernate.connection.password";

    private final Properties props;
    private final Configuration cfg = new Configuration();
    private final Configuration cfgManaged = new Configuration();

    public HibernateConfigurationFactory(final Properties props, final HibernateMappingsGenerator generator) {
        this.props = props;
        // TODO use declarative style
        // Register our custom type mapping so that Hibernate uses it during the binding of query parameters.
        cfg.registerTypeContributor((typeContributions, $) -> typeContributions.contributeType(DateTimeType.INSTANCE));

        final String generatedMappings = generator.generateMappings();
        try {
            cfg.addInputStream(new ByteArrayInputStream(generatedMappings.getBytes(UTF_8)));
            cfgManaged.addInputStream(new ByteArrayInputStream(generatedMappings.getBytes(UTF_8)));
        } catch (final MappingException e) {
            throw new HibernateException("Could not add mappings.", e);
        }
    }

    public static DbVersion determineDbVersion(final Properties props) {
        return determineDbVersion(props.getProperty(DIALECT));
    }

    public static DbVersion determineDbVersion(final String dialect) {
        if (isEmpty(dialect)) {
            throw new InvalidArgumentException("Hibernate dialect was not provided, but is required");
        }
        return HibernateHelpers.getDbVersion(HibernateHelpers.getDialect(dialect));
    }

    public Configuration build() {
        setSafely(cfg, "hibernate.current_session_context_class", "thread");

        setSafely(cfg, SHOW_SQL, "false");
        setSafely(cfg, FORMAT_SQL, "true");
        setSafely(cfg, JDBC_USE_GET_GENERATED_KEYS, "true");

        setSafely(cfg, CONNECTION_PROVIDER_CLASS, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        setAutoCommitSettings(props, cfg);

        setSafely(cfg, C3P0_NUM_HELPER_THREADS);
        setSafely(cfg, C3P0_MIN_SIZE);
        setSafely(cfg, C3P0_MAX_SIZE);
        setSafely(cfg, C3P0_TIMEOUT);
        setSafely(cfg, C3P0_MAX_STATEMENTS);
        setSafely(cfg, C3P0_ACQUIRE_INCREMENT);
        setSafely(cfg, C3P0_IDLE_TEST_PERIOD);

        setSafely(cfg, HIKARI_CONNECTION_TIMEOUT, "30000"); // 30 seconds
        setSafely(cfg, HIKARI_MIN_SIZE); // nothing, allowing HikariCP to do its thing
        setSafely(cfg, HIKARI_MAX_SIZE, "10"); // 10 connections are plenty in most cases
        setSafely(cfg, HIKARI_IDLE_TIMEOUT, "240000"); // 4 minutes
        setSafely(cfg, HIKARI_MAX_LIFETIME, "270000"); // 4 minutes and 30 seconds

        setSafely(cfg, HBM2DDL_AUTO);

        setSafely(cfg, CONNECTION_URL);
        setSafely(cfg, CONNECTION_DRIVER_CLASS);
        setSafely(cfg, DIALECT);
        setSafely(cfg, CONNECTION_USERNAME, "");
        setSafely(cfg, CONNECTION_PASWD, "");

        return cfg;
    }

    /// Pooled connections are handed out with auto-commit disabled, and Hibernate is told so by `hibernate.connection.provider_disables_autocommit`.
    /// Beginning a transaction then involves no exchange with the database: Hibernate neither acquires a connection nor disables auto-commit on it.
    /// The connection is acquired by the first statement of a unit of work, within the error handling of `SessionInterceptor`,
    /// which can discard a connection that a [VirtualMachineError] may have left out of sync with the server.
    /// A failure while beginning a transaction is beyond the reach of that error handling, as the thread-bound session gives no access to its connection until its transaction is active.
    /// With SQL Server, this also saves two round trips per transaction, as its JDBC driver changes the auto-commit mode by executing a statement.
    ///
    /// Both supported connection pool providers apply `hibernate.connection.autocommit` to the connections they hand out.
    /// HikariCP gives precedence to `hibernate.hikari.autoCommit`, as `HikariConfigurationUtil` copies every `hibernate.hikari.*` property after `hibernate.connection.autocommit`,
    /// so that property is set to `false` too.
    /// Hibernate relies on `hibernate.connection.provider_disables_autocommit` without checking:
    /// if connections were handed out with auto-commit enabled, each statement would be committed on its own.
    /// A connection provider that does not apply `hibernate.connection.autocommit`, such as one that supplies a `DataSource` via `hibernate.connection.datasource`,
    /// cannot be used with these settings: its connections would have auto-commit enabled, while Hibernate would treat them as having it disabled.
    ///
    /// These settings are fixed, and specifying any of them is rejected, so that the configuration cannot reintroduce an exchange with the database when beginning a transaction.
    /// The configuration starts from the properties of Hibernate's `Environment`, which include JVM system properties and a `hibernate.properties` resource on the classpath.
    /// The properties set here take precedence over those, as they are applied last when the session factory is built, so those sources cannot change these settings either.
    /// For application code, the settings matter only outside transactions managed by Hibernate, where pooled connections do not commit automatically.
    /// Database access in TG goes through transactions managed by Hibernate, which commit explicitly.
    /// Application code that obtains pooled connections in any other way must not rely on them committing automatically.
    ///
    static void setAutoCommitSettings(final Properties props, final Configuration cfg) {
        for (final String property : new String[] {CONNECTION_AUTOCOMMIT, CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, HIKARI_AUTO_COMMIT}) {
            if (props.getProperty(property) != null) {
                throw new InvalidArgumentException(ERR_AUTOCOMMIT_SETTING_SPECIFIED.formatted(property));
            }
        }
        cfg.setProperty(CONNECTION_AUTOCOMMIT, "false");
        cfg.setProperty(CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, "true");
        cfg.setProperty(HIKARI_AUTO_COMMIT, "false");
    }

    private Configuration setSafely(final Configuration cfg, final String propertyName, final String defaultValue) {
        final String value = props.getProperty(propertyName);
        if (value != null) {
            cfg.setProperty(propertyName, value);
        } else if (defaultValue != null) {
            cfg.setProperty(propertyName, defaultValue);
        }
        return cfg;
    }

    private Configuration setSafely(final Configuration cfg, final String propertyName) {
        return setSafely(cfg, propertyName, null);
    }
}
