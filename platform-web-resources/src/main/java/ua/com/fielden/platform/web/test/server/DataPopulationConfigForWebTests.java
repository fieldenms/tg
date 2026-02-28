package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

/// Provides Web UI Testing Server specific implementation of [IDomainDrivenTestCaseConfiguration] to be used for web unit tests.
///
public final class DataPopulationConfigForWebTests implements IDomainDrivenTestCaseConfiguration {
    private final Injector injector;

    public DataPopulationConfigForWebTests(final Properties props) {
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
            props.setProperty("email.smtp", "localhost");
            props.setProperty("email.fromAddress", "tg@localhost");
            props.setProperty("audit.path", "../platform-pojo-bl/target/classes");

            final ApplicationDomain appDomain = new ApplicationDomain();
            injector = new ApplicationInjectorFactory()
                .add(new TgTestWebApplicationServerIocModule(appDomain, appDomain.domainTypes(), props))
                .add(new NewUserEmailNotifierTestIocModule())
                .getInjector();
        } catch (final Exception e) {
            throw new IllegalStateException("Could not create data population configuration.", e);
        }
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

}
