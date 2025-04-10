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
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Hibernate configuration factory.
 * All Hibernate specific properties should be passed as {@link Properties} values.
 * <h4>DB connection</h4>
 * <ul>
 * <li><i><font color="981515">hibernate.connection.url</font></i> – required;
 * <li><i><font color="981515">hibernate.connection.driver_class</font></i> – required;
 * <li><i><font color="981515">hibernate.dialect</font></i> – required;
 * <li><i><font color="981515">hibernate.connection.username</font></i> – required;
 * <li><i><font color="981515">hibernate.connection.password</font></i> – required;
 * <li><i>hibernate.show_sql</i> – defaults to {@code false};
 * <li><i>hibernate.format_sql</i> – defaults to {@code false};
 * </ul>
 * <h4>DB connection pool providers and their properties</h4>
 * <i>hibernate.connection.provider_class</i> – HikariCP {@code org.hibernate.hikaricp.internal.HikariCPConnectionProvider} is used by default; c3p0 {@code org.hibernate.connection.C3P0ConnectionProvider} is also supported;
 *
 * <h5>HikariCP configuration properties</h5>
 * Refer to the official <a href='https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby'>Gear Configuration</a> for more details.
 * <ul>
 * <li><i>hibernate.hikari.connectionTimeout</i> – a maximum waiting time in millis for a connection from the pool; defaults to 3000 (30 seconds);
 * <li><i>hibernate.hikari.minimumIdle</i> -- a minimum number of ideal connections in the pool; defaults to the same value as maximumPoolSize;
 * <li><i>hibernate.hikari.maximumPoolSize</i> -- a maximum number of actual connections in the pool; defaults to 10 (refer <a href='https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing">About Pool Sizing</a> for more information);
 * <li><i>hibernate.hikari.idleTimeout</i> -- a maximum time in millis that a connection is allowed to sit idle in the pool; defaults to 240000 (4 minutes), which is suitable for Azure SQL;
 * <li><i>hibernate.hikari.maxLifetime</i> -- a maximum lifetime in millis of a connection in the pool; defaults to 270000 (4:30 minutes).
 * </ul>
 * <h5>c3p0 configuration properties</h5>
 * <ul>
 * <li><i>hibernate.c3p0.min_size</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>hibernate.c3p0.max_size</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>hibernate.c3p0.timeout</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>hibernate.c3p0.max_statements</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>hibernate.c3p0.acquire_increment</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>hibernate.c3p0.idle_test_period</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * </ul>
 *
 * @author TG Team
 *
 */
public class HibernateConfigurationFactory {

    private static final String SHOW_SQL = "hibernate.show_sql";
    private static final String FORMAT_SQL = "hibernate.format_sql";
    private static final String JDBC_USE_GET_GENERATED_KEYS = "hibernate.jdbc.use_get_generated_keys";
    private static final String CONNECTION_PROVIDER_CLASS = "hibernate.connection.provider_class";

    // C3P0 connection pool settings
    private static final String C3P0_NUM_HELPER_THREADS = "hibernate.c3p0.numHelperThreads";
    private static final String C3P0_MIN_SIZE = "hibernate.c3p0.min_size";
    private static final String C3P0_MAX_SIZE = "hibernate.c3p0.max_size";
    private static final String C3P0_TIMEOUT = "hibernate.c3p0.timeout";
    private static final String C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements";
    private static final String C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
    private static final String C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";

    // Hikari connection pool settings
    private static final String HIKARI_CONNECTION_TIMEOUT = "hibernate.hikari.connectionTimeout";
    private static final String HIKARI_MIN_SIZE = "hibernate.hikari.minimumIdle";
    private static final String HIKARI_MAX_SIZE = "hibernate.hikari.maximumPoolSize";
    private static final String HIKARI_IDLE_TIMEOUT = "hibernate.hikari.idleTimeout";
    private static final String HIKARI_MAX_LIFETIME = "hibernate.hikari.maxLifetime";

    private static final String HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    private static final String CONNECTION_URL = "hibernate.connection.url";
    private static final String CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
    private static final String DIALECT = "hibernate.dialect";
    private static final String CONNECTION_USERNAME = "hibernate.connection.username";
    private static final String CONNECTION_PASWD = "hibernate.connection.password";

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
            cfg.addInputStream(new ByteArrayInputStream(generatedMappings.getBytes("UTF8")));
            cfgManaged.addInputStream(new ByteArrayInputStream(generatedMappings.getBytes("UTF8")));
        } catch (final MappingException | UnsupportedEncodingException e) {
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

        setSafely(cfg, C3P0_NUM_HELPER_THREADS);
        setSafely(cfg, C3P0_MIN_SIZE);
        setSafely(cfg, C3P0_MAX_SIZE);
        setSafely(cfg, C3P0_TIMEOUT);
        setSafely(cfg, C3P0_MAX_STATEMENTS);
        setSafely(cfg, C3P0_ACQUIRE_INCREMENT);
        setSafely(cfg, C3P0_IDLE_TEST_PERIOD);

        setSafely(cfg, HIKARI_CONNECTION_TIMEOUT, "3000"); // 30 seconds
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
