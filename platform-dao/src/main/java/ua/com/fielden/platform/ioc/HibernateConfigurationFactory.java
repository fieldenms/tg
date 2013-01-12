package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.DbVersion;

import com.google.inject.Guice;

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
 * <li><i>c3p0.min_size</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>c3p0.max_size</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>c3p0.timeout</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>c3p0.max_statements</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>c3p0.acquire_increment</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li><i>c3p0.idle_test_period</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * </ul>
 * 
 * @author TG Team
 * 
 */
public class HibernateConfigurationFactory {

    private final Properties props;
    private final DomainMetadata domainMetadata;
    private final Configuration cfg = new Configuration();
    private final Configuration cfgManaged = new Configuration();

    public HibernateConfigurationFactory(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes)
	    throws Exception {
	this.props = props;
	domainMetadata = new DomainMetadata(defaultHibernateTypes, Guice.createInjector(new HibernateUserTypesModule()), applicationEntityTypes, determineDbVersion(props));
	cfg.addXML(new HibernateMappingsGenerator().generateMappings(domainMetadata.getEntityMetadatas()));
	cfgManaged.addXML(new HibernateMappingsGenerator().generateMappings(domainMetadata.getEntityMetadatas()));
    }

    private DbVersion determineDbVersion(final Properties props) {
	final String dialect = props.getProperty("hibernate.dialect");
	if (dialect.equals("org.hibernate.dialect.H2Dialect")) {
	    return DbVersion.H2;
	} else if (dialect.equals("org.hibernate.dialect.PostgreSQLDialect")) {
	    return DbVersion.POSTGRESQL;
	} else if (dialect.equals("org.hibernate.dialect.SQLServerDialect")) {
	    return DbVersion.MSSQL;
	}
	throw new IllegalStateException("Could not determine DB version based on the provided Hibernate dialect \"" + dialect + "\".");
    }

    public Configuration build() {
	cfg.setProperty("hibernate.current_session_context_class", "thread");

	setSafely(cfg, "hibernate.show_sql", "false");
	setSafely(cfg, "hibernate.format_sql", "true");

	setSafely(cfg, "hibernate.connection.provider_class");
	setSafely(cfg, "c3p0.min_size");
	setSafely(cfg, "c3p0.max_size");
	setSafely(cfg, "c3p0.timeout");
	setSafely(cfg, "c3p0.max_statements");
	setSafely(cfg, "c3p0.acquire_increment");
	setSafely(cfg, "c3p0.idle_test_period");
	setSafely(cfg, "hibernate.hbm2ddl.auto");

	setSafely(cfg, "hibernate.connection.url");
	setSafely(cfg, "hibernate.connection.driver_class");
	setSafely(cfg, "hibernate.dialect");
	setSafely(cfg, "hibernate.connection.username", "");
	setSafely(cfg, "hibernate.connection.password", "");

	return cfg;
    }

    public Configuration buildManaged() {

	cfgManaged.setProperty("hibernate.current_session_context_class", "managed");

	setSafely(cfgManaged, "hibernate.show_sql", "false");
	setSafely(cfgManaged, "hibernate.format_sql", "true");

	setSafely(cfgManaged, "hibernate.connection.provider_class");
	setSafely(cfgManaged, "c3p0.min_size");
	setSafely(cfgManaged, "c3p0.max_size");
	setSafely(cfgManaged, "c3p0.timeout");
	setSafely(cfgManaged, "c3p0.max_statements");
	setSafely(cfgManaged, "c3p0.acquire_increment");
	setSafely(cfgManaged, "c3p0.idle_test_period");
	setSafely(cfgManaged, "hibernate.hbm2ddl.auto");

	setSafely(cfgManaged, "hibernate.connection.url");
	setSafely(cfgManaged, "hibernate.connection.driver_class");
	setSafely(cfgManaged, "hibernate.dialect");
	setSafely(cfgManaged, "hibernate.connection.username", "");
	setSafely(cfgManaged, "hibernate.connection.password", "");

	return cfgManaged;
    }

    public DomainMetadata getDomainMetadata() {
	return domainMetadata;
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
