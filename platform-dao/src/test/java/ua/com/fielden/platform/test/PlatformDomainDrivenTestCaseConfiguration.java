package ua.com.fielden.platform.test;

import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.DefaultFilter;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.serialisation.api.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.test.ioc.PlatformTestServerModule;

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
    public PlatformDomainDrivenTestCaseConfiguration(final Properties hbc) {
        // instantiate all the factories and Hibernate utility
        try {
            DOMConfigurator.configure("src/test/resources/log4j.xml");
            final Properties props = new Properties(hbc);
            // application properties
            props.setProperty("app.name", "TG Test");
            props.setProperty("reports.path", "");
            props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
            props.setProperty("domain.package", "ua.com.fielden.platform");
            props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
            props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
            props.setProperty("workflow", "development");
            // Custom Hibernate configuration properties
            props.setProperty("hibernate.show_sql", "false");
            props.setProperty("hibernate.format_sql", "true");
            props.setProperty("attachments.location", "src/test/resources/attachments");
            props.setProperty("email.smtp", "non-existing-server");
            props.setProperty("email.fromAddress", "platform@fielden.com.au");


            final PlatformTestDomainTypes domainProvider = new PlatformTestDomainTypes();

            hibernateModule = new PlatformTestServerModule(PlatformTestHibernateSetup.getHibernateTypes(), domainProvider, DefaultSerialisationClassProvider.class, DefaultFilter.class, props);
            injector = new ApplicationInjectorFactory().add(hibernateModule).add(new NewUserNotifierMockBindingModule()).getInjector();

            entityFactory = injector.getInstance(EntityFactory.class);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

    @Override
    public DomainMetadata getDomainMetadata() {
        return hibernateModule.getDomainMetadata();
    }
    
    @Override
    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return hibernateModule.getIdOnlyProxiedEntityTypeCache();
    }
}