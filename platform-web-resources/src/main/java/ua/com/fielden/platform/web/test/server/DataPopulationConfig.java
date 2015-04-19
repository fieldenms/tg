package ua.com.fielden.platform.web.test.server;

import java.util.Properties;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

import com.google.inject.Injector;

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
    private final ApplicationServerModule module;

    /**
     * Default constructor is required for dynamic instantiation by {@link DbDrivenTestCase}.
     */
    public DataPopulationConfig() {
        // instantiate all the factories and Hibernate utility
        try {
            final Properties props = hbc;
            // application properties
            props.setProperty("app.home", "");
            props.setProperty("reports.path", "");
            props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
            props.setProperty("domain.package", "ua.com.fielden.platform.sample.domain");
            props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
            props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
            props.setProperty("workflow", "development");

            final ApplicationDomain applicationDomainProvider = new ApplicationDomain();
            module = new ApplicationServerModule(HibernateSetup.getHibernateTypes(), applicationDomainProvider, applicationDomainProvider.domainTypes(), SerialisationClassProvider.class, NoDataFilter.class, props);
            injector = new ApplicationInjectorFactory().add(module).getInjector();
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
    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
        return module.getDomainMetaPropertyConfig();
    }

    @Override
    public DomainValidationConfig getDomainValidationConfig() {
        return module.getDomainValidationConfig();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

    @Override
    public DomainMetadata getDomainMetadata() {
        return module.getDomainMetadata();
    }
}