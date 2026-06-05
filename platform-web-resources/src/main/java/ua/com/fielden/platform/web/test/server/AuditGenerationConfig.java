package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_PATH;
import static ua.com.fielden.platform.utils.MiscUtilities.propertiesUnionLeft;

/// Configures the test application to enable generation of audit types.
///
public final class AuditGenerationConfig implements IDomainDrivenTestCaseConfiguration {

    private final Injector injector;

    /// Creates a configuration using the provided properties.
    /// Some default properties are set by this constructor, but the provided ones take precedence.
    ///
    public AuditGenerationConfig(final Properties props) {
        try {
            // application properties
            final var defaultProps = new Properties();
            defaultProps.setProperty("app.name", "TG Test App");
            defaultProps.setProperty("reports.path", "");
            defaultProps.setProperty("domain.path", "../platform-pojo-bl/target/classes");
            defaultProps.setProperty("domain.package", "ua.com.fielden.platform.sample.domain");
            defaultProps.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
            defaultProps.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
            defaultProps.setProperty(AUDIT_PATH, "../platform-pojo-bl/target/classes");
            defaultProps.setProperty(AUDIT_MODE, AuditingMode.GENERATION.name());
            defaultProps.setProperty("workflow", "development");
            defaultProps.setProperty("email.smtp", "localhost");
            defaultProps.setProperty("email.fromAddress", "tg@localhost");

            final var finalProps = propertiesUnionLeft(props, defaultProps);

            final var appDomain = new ApplicationDomain();
            injector = new ApplicationInjectorFactory()
                    .add(new TgTestApplicationServerIocModule(appDomain, appDomain.domainTypes(), finalProps))
                    .add(new NewUserEmailNotifierTestIocModule())
                    .add(new DataFilterTestIocModule())
                    .getInjector();
        } catch (final Exception e) {
            throw new InvalidStateException("Could not create audit generation configuration.", e);
        }
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

}
