package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.DefaultUniversalConstants;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

/**
 * Provides Web UI Testing Server specific implementation of {@link IDomainDrivenTestCaseConfiguration} to be used for creation and population of the target development database
 * from within of IDE.
 *
 * @author TG Team
 *
 */
public final class DataPopulationConfig implements IDomainDrivenTestCaseConfiguration {
    private final Injector injector;

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
            props.setProperty("email.smtp", "localhost");
            props.setProperty("email.fromAddress", "tg@localhost");

            final ApplicationDomain appDomain = new ApplicationDomain();
            injector = new ApplicationInjectorFactory()
                    .add(new TgTestApplicationServerModule(appDomain,
                                                           appDomain.domainTypes(),
                                                           DefaultUniversalConstants.class,
                                                           DefaultDates.class, props))
                    .add(new NewUserNotifierMockBindingModule())
                    .add(new DataFilterModule())
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
