package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_PATH;

/**
 * Configures the test application to enable generation of audit types.
 *
 * @author TG Team
 */
public final class AuditGenerationConfig implements IDomainDrivenTestCaseConfiguration {

    private final Injector injector;

    public AuditGenerationConfig(final Properties props) {
        try {
            // application properties
            props.setProperty("app.name", "TG Test App");
            props.setProperty("reports.path", "");
            props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
            props.setProperty("domain.package", "ua.com.fielden.platform.sample.domain");
            props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
            props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
            props.setProperty(AUDIT_PATH, "../platform-pojo-bl/target/classes");
            props.setProperty(AUDIT_MODE, AuditingMode.GENERATION.name());
            props.setProperty("workflow", "development");
            props.setProperty("email.smtp", "localhost");
            props.setProperty("email.fromAddress", "tg@localhost");

            final var appDomain = new ApplicationDomain();
            injector = new ApplicationInjectorFactory()
                    .add(new TgTestApplicationServerIocModule(appDomain, appDomain.domainTypes(), props))
                    .add(new NewUserEmailNotifierTestIocModule())
                    .add(new DataFilterTestIocModule())
                    .getInjector();
        } catch (final Exception e) {
            throw new IllegalStateException("Could not create audit generation configuration.", e);
        }
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

}
