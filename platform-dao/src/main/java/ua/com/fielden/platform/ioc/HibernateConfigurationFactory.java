package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.dao.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;

import com.google.inject.Guice;

/**
 * Hibernate configuration factory. All Hibernate specific properties should be passed as {@link Properties} values.
 * The following list of properties is supported:
 * <ul>
 * <li> <i><font color="981515">hibernate.connection.url</font></i> -- required;
 * <li> <i><font color="981515">hibernate.connection.driver_class</font></i> -- required;
 * <li> <i><font color="981515">hibernate.dialect</font></i> -- required;
 * <li> <i><font color="981515">hibernate.connection.username</font></i> -- required;
 * <li> <i><font color="981515">hibernate.connection.password</font></i> -- required;
 * <li>	<i>hibernate.show_sql</i> -- defaults to "true";
 * <li> <i>hibernate.format_sql</i> -- defaults to "true";
 * <li> <i>hibernate.connection.provider_class</i> -- if provided value org.hibernate.connection.C3P0ConnectionProvider is expected; other types of pulls are not yet supported;
 * <li> <i>c3p0.min_size</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li> <i>c3p0.max_size</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li> <i>c3p0.timeout</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li> <i>c3p0.max_statements</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li> <i>c3p0.acquire_increment</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * <li> <i>c3p0.idle_test_period</i> -- should accompany the C3P0ConnectionProvider in case it is specified;
 * </ul>
 *
 * @author TG Team
 *
 */
public class HibernateConfigurationFactory {

    private final Properties props;
    private final DomainPersistenceMetadata domainPersistenceMetadata;
    private final Configuration cfg = new Configuration();

    public HibernateConfigurationFactory(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) throws Exception {
	this.props = props;
	domainPersistenceMetadata = new DomainPersistenceMetadata(defaultHibernateTypes, Guice.createInjector(new HibernateUserTypesModule()), applicationEntityTypes);
	if (domainPersistenceMetadata != null) {
	    cfg.addXML(new HibernateMappingsGenerator(domainPersistenceMetadata.getHibTypeInfosMap()).generateMappings());
	}
    }

    public Configuration build() {
	cfg.setProperty("hibernate.current_session_context_class", "thread");

	setSafely("hibernate.show_sql", "false");
	setSafely("hibernate.format_sql", "true");

	setSafely("hibernate.connection.provider_class");
	setSafely("c3p0.min_size");
	setSafely("c3p0.max_size");
	setSafely("c3p0.timeout");
	setSafely("c3p0.max_statements");
	setSafely("c3p0.acquire_increment");
	setSafely("c3p0.idle_test_period");
	setSafely("hibernate.hbm2ddl.auto");

	setSafely("hibernate.connection.url");
	setSafely("hibernate.connection.driver_class");
	setSafely("hibernate.dialect");
	setSafely("hibernate.connection.username", "");
	setSafely("hibernate.connection.password", "");

	return cfg;
    }

    public DomainPersistenceMetadata getDomainPersistenceMetadata() {
	return domainPersistenceMetadata;
    }

    private Configuration setSafely(final String propertyName, final String defaultValue) {
	final String value = props.getProperty(propertyName);
	if (value != null) {
	    cfg.setProperty(propertyName, value);
	} else if (defaultValue != null) {
	    cfg.setProperty(propertyName, defaultValue);
	}
	return cfg;
    }

    private Configuration setSafely(final String propertyName) {
	return setSafely(propertyName, null);
    }
}
