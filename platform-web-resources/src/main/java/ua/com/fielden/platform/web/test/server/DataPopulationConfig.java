package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Optional;
import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;
import static ua.com.fielden.platform.utils.MiscUtilities.propertiesUnionLeft;

/**
 * Provides Web UI Testing Server specific implementation of {@link IDomainDrivenTestCaseConfiguration}
 * to be used for creation and population of the target development database.
 *
 * @author TG Team
 */
public final class DataPopulationConfig implements IDomainDrivenTestCaseConfiguration {

    private final Injector injector;

    /// Creates a configuration using the provided properties.
    /// Some default properties are set by this constructor, but the provided ones take precedence.
    public DataPopulationConfig(final Properties props) {
        // instantiate all the factories and Hibernate utility
        try {
            // application properties
            final var defaultProps = new Properties();
            defaultProps.setProperty("app.name", "TG Test App");
            defaultProps.setProperty("reports.path", "");
            defaultProps.setProperty("domain.path", "../platform-pojo-bl/target/classes");
            defaultProps.setProperty("domain.package", "ua.com.fielden.platform.sample.domain");
            defaultProps.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
            defaultProps.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
            defaultProps.setProperty(AUDIT_MODE, AuditingMode.DISABLED.name());
            defaultProps.setProperty("workflow", "development");
            defaultProps.setProperty("email.smtp", "localhost");
            defaultProps.setProperty("email.fromAddress", "tg@localhost");

            final var finalProps = propertiesUnionLeft(props, defaultProps);

            final ApplicationDomain appDomain = new ApplicationDomain();
            injector = new ApplicationInjectorFactory()
                    .add(new TgTestApplicationServerIocModule(appDomain, appDomain.domainTypes(), finalProps))
                    .add(new NewUserEmailNotifierTestIocModule())
                    .add(new DataFilterTestIocModule())
                    .add(new IocModule())
                    .getInjector();
        } catch (final Exception e) {
            throw new IllegalStateException("Could not create data population configuration.", e);
        }
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

    static class IocModule extends AbstractPlatformIocModule {

        @Override
        protected void configure() {
            // Requires a binding, but is not used for data population.
            bind(IEntityMasterUrlProvider.class).toInstance(_ -> Optional.empty());
        }

    }

}
