package ua.com.fielden.platform.test;

import java.util.Properties;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.DefaultFilter;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
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

    public PlatformDomainDrivenTestCaseConfiguration(final Properties hbc) {
        // instantiate all the factories and Hibernate utility
        try {
            final Properties props = getProperties(hbc);
            final PlatformTestDomainTypes domainProvider = new PlatformTestDomainTypes();

            hibernateModule = new PlatformTestServerModule(PlatformTestHibernateSetup.getHibernateTypes(), domainProvider, DefaultSerialisationClassProvider.class, DefaultFilter.class, SecurityTokenProvider.class, props);
            injector = new ApplicationInjectorFactory().add(hibernateModule).add(new NewUserNotifierMockBindingModule()).getInjector();

            entityFactory = injector.getInstance(EntityFactory.class);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Properties getProperties(final Properties hbc) {
        final Properties props = new Properties(hbc);
        // application properties
        props.setProperty("workflow", "development");
        props.setProperty("app.name", "TG Test");
        props.setProperty("reports.path", "");
        props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
        props.setProperty("domain.package", "ua.com.fielden.platform");
        props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
        props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
        props.setProperty("attachments.location", "src/test/resources/attachments");
        props.setProperty("email.smtp", "non-existing-server");
        props.setProperty("email.fromAddress", "platform@fielden.com.au");
        props.setProperty("web.api", "true");
        // Custom Hibernate configuration properties
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");
        return props;
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