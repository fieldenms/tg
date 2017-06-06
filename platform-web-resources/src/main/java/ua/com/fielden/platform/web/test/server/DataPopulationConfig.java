package ua.com.fielden.platform.web.test.server;

import java.util.Properties;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

/**
 * Provides Web UI Testing Server specific implementation of {@link IDomainDrivenTestCaseConfiguration} to be used for creation and population of the target development database
 * from within of IDE.
 *
 * @author TG Team
 *
 */
public final class DataPopulationConfig implements IDomainDrivenTestCaseConfiguration {
    private final EntityFactory entityFactory;
    private final Injector injector;
    private final TgTestApplicationServerModule module;

    /**
     * Default constructor is required for dynamic instantiation by {@link DbDrivenTestCase}.
     */
    public DataPopulationConfig(final Properties props) {
        // instantiate all the factories and Hibernate utility
        try {
            // application properties
            props.setProperty("app.name", "TG Test App");
            props.setProperty("reports.path", "");
            props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
            props.setProperty("domain.package", "ua.com.fielden.platform.sample.domain");
            props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
            props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
            props.setProperty("workflow", "development");
            props.setProperty("email.smtp", "non-existing-server");
            props.setProperty("email.fromAddress", "tg@fielden.com.au");

            final ApplicationDomain applicationDomainProvider = new ApplicationDomain();
            module = new TgTestApplicationServerModule(HibernateSetup.getHibernateTypes(), applicationDomainProvider, applicationDomainProvider.domainTypes(), SerialisationClassProvider.class, ExampleDataFilter.class, props);
            injector = new ApplicationInjectorFactory().add(module).add(new NewUserNotifierMockBindingModule()).getInjector();
            entityFactory = injector.getInstance(EntityFactory.class);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not create data population configuration.", e);
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
        return module.getDomainMetadata();
    }

    @Override
    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return module.getIdOnlyProxiedEntityTypeCache();
    }
}