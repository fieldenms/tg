package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;

/// Provides Web UI Testing Server specific implementation of [IDomainDrivenTestCaseConfiguration] to be used for web unit tests.
///
public final class DataPopulationConfigForWebTests extends DataPopulationConfig {

    /// Creates a configuration using the provided properties.
    /// Some default properties are set by this constructor, but the provided ones take precedence.
    ///
    public DataPopulationConfigForWebTests(final Properties props) {
        super(enableAuditing(props));
    }

    /// Enables auditing, which is required by Web UI in `TgTestWebApplicationServerIocModule`.
    ///
    private static Properties enableAuditing(final Properties props) {
        props.setProperty(AUDIT_MODE, AuditingMode.ENABLED.name());
        props.setProperty("audit.path", "../platform-pojo-bl/target/classes");
        return props;
    }

    @Override
    protected ApplicationInjectorFactory createFactory(final Properties properties) {
        final ApplicationDomain appDomain = new ApplicationDomain();
        return new ApplicationInjectorFactory()
            .add(new TgTestWebApplicationServerIocModule(appDomain, appDomain.domainTypes(), properties))
            .add(new NewUserEmailNotifierTestIocModule())
            .add(new DataFilterTestIocModule())
            .add(new UniversalConstantsTestIocModule());
    }

}
