package ua.com.fielden.platform.test;

import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.query.DefaultFilter;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.equery.Rdbms;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.test.ioc.PlatformTestServerModule;

import com.google.inject.Injector;

/**
 * Provides Platform specific implementation of {@link IDomainDrivenTestCaseConfiguration} for testing purposes, which is mainly related to construction of appropriate IoC modules.
 *
 * @author TG Team
 *
 */
public final class PlatformDomainDrivenTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {
    private final EntityFactory entityFactory;
    private final Injector injector;
    private final PlatformTestServerModule hibernateModule;

    /**
     * Required for dynamic instantiation by {@link DbDrivenTestCase}
     */
    public PlatformDomainDrivenTestCaseConfiguration() {
	// instantiate all the factories and Hibernate utility
	try {
	    DOMConfigurator.configure("src/test/resources/log4j.xml");
	    Rdbms.rdbms = Rdbms.H2;
	    final Properties props = new Properties(hbc);
	    // application properties
	    props.setProperty("app.home", "");
	    props.setProperty("reports.path", "");
	    props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
	    props.setProperty("domain.package", "ua.com.fielden.platform");
	    props.setProperty("private-key", "");
	    props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
	    props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
	    props.setProperty("workflow", "development");
	    // Custom Hibernate configuration properties
	    props.setProperty("hibernate.show_sql", "false");
	    props.setProperty("hibernate.format_sql", "true");

	    hibernateModule = new PlatformTestServerModule(
		    PlatformTestHibernateSetup.getHibernateTypes(),
		    PlatformTestDomainTypes.entityTypes,
		    DefaultSerialisationClassProvider.class,
		    DefaultFilter.class,
		    props);
	    injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();

	    entityFactory = injector.getInstance(EntityFactory.class);

	    // bind domain specific validation classes
	    bindDomainValidation(hibernateModule);
	    // bind domain specific meta property configuration classes
	    bindDomainMetaProperty(hibernateModule);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    /**
     * Binds domain specific configurators for meta properties on entity property.
     *
     * @param hibernateUtil
     */
    private void bindDomainMetaProperty(final PlatformTestServerModule hibernateModule) {
	// TODO Add domain meta property binding if needed
    }

    /**
     * Binds entity property domain validation logic.
     *
     * @param hibernateUtil
     */
    private void bindDomainValidation(final PlatformTestServerModule hibernateModule) {
	// TODO Add domain validation binding if needed
    }

    @Override
    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    @Override
    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
	return hibernateModule.getDomainMetaPropertyConfig();
    }

    @Override
    public DomainValidationConfig getDomainValidationConfig() {
	return hibernateModule.getDomainValidationConfig();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
	return injector.getInstance(type);
    }

    public DomainMetadata getDomainMetadata() {
	return hibernateModule.getDomainMetadata();
    }
}