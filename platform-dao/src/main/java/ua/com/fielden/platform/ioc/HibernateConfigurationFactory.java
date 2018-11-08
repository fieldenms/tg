package ua.com.fielden.platform.ioc;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;

import com.google.inject.Guice;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;

/**
 * Hibernate configuration factory. All Hibernate specific properties should be passed as {@link Properties} values. The following list of properties is supported:
 * <ul>
 * <li><i><font color="981515">hibernate.connection.url</font></i> -- required;
 * <li><i><font color="981515">hibernate.connection.driver_class</font></i> -- required;
 * <li><i><font color="981515">hibernate.dialect</font></i> -- required;
 * <li><i><font color="981515">hibernate.connection.username</font></i> -- required;
 * <li><i><font color="981515">hibernate.connection.password</font></i> -- required;
 * <li><i>hibernate.show_sql</i> -- defaults to "true";
 * <li><i>hibernate.format_sql</i> -- defaults to "true";
 * <li><i>hibernate.connection.provider_class</i> -- if provided value org.hibernate.connection.C3P0ConnectionProvider is expected; other types of pulls are not yet supported;
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
    private static final String C3P0_NUM_HELPER_THREADS = "hibernate.c3p0.numHelperThreads";
    private static final String C3P0_MIN_SIZE = "hibernate.c3p0.min_size";
    private static final String C3P0_MAX_SIZE = "hibernate.c3p0.max_size";
    private static final String C3P0_TIMEOUT = "hibernate.c3p0.timeout";
    private static final String C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements";
    private static final String C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
    private static final String C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";
    private static final String HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    private static final String CONNECTION_URL = "hibernate.connection.url";
    private static final String CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
    private static final String DIALECT = "hibernate.dialect";
    private static final String CONNECTION_USERNAME = "hibernate.connection.username";
    private static final String CONNECTION_PASWD = "hibernate.connection.password";

    private final Properties props;
    private final DomainMetadata domainMetadata;
    private final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;

    private final Configuration cfg = new Configuration();
    private final Configuration cfgManaged = new Configuration();

    public HibernateConfigurationFactory(//
            final Properties props, //
            final Map<Class, Class> defaultHibernateTypes, //
            final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) {
        this.props = props;

        domainMetadata = new DomainMetadata(//
                defaultHibernateTypes,//
                Guice.createInjector(new HibernateUserTypesModule()), //
                applicationEntityTypes, //
                determineDbVersion(props));
        
        idOnlyProxiedEntityTypeCache = new IdOnlyProxiedEntityTypeCache(domainMetadata);

        final String generatedMappings = new HibernateMappingsGenerator().generateMappings(domainMetadata);
        
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
            throw new IllegalStateException("Hibernate dialect was not provided, but is required");
        }
        if (dialect.equals("org.hibernate.dialect.H2Dialect")) {
            return DbVersion.H2;
        } else if (dialect.equals("org.hibernate.dialect.PostgreSQLDialect")) {
            return DbVersion.POSTGRESQL;
        } else if (dialect.contains("SQLServer")) {
            return DbVersion.MSSQL;
        } else if (dialect.equals("org.hibernate.dialect.OracleDialect")) {
            return DbVersion.ORACLE;
        }

        throw new IllegalStateException("Could not determine DB version based on the provided Hibernate dialect \"" + dialect + "\".");
    }

    public Configuration build() {
        setSafely(cfg, "hibernate.current_session_context_class", "thread");

        setSafely(cfg, SHOW_SQL, "false");
        setSafely(cfg, FORMAT_SQL, "true");
        setSafely(cfg, JDBC_USE_GET_GENERATED_KEYS, "true");

        setSafely(cfg, CONNECTION_PROVIDER_CLASS);
        setSafely(cfg, C3P0_NUM_HELPER_THREADS);
        setSafely(cfg, C3P0_MIN_SIZE);
        setSafely(cfg, C3P0_MAX_SIZE);
        setSafely(cfg, C3P0_TIMEOUT);
        setSafely(cfg, C3P0_MAX_STATEMENTS);
        setSafely(cfg, C3P0_ACQUIRE_INCREMENT);
        setSafely(cfg, C3P0_IDLE_TEST_PERIOD);
        setSafely(cfg, HBM2DDL_AUTO);

        setSafely(cfg, CONNECTION_URL);
        setSafely(cfg, CONNECTION_DRIVER_CLASS);
        setSafely(cfg, DIALECT);
        setSafely(cfg, CONNECTION_USERNAME, "");
        setSafely(cfg, CONNECTION_PASWD, "");

        return cfg;
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }

    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return idOnlyProxiedEntityTypeCache;
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
