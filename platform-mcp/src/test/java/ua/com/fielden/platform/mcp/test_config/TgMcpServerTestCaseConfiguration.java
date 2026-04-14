package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.Injector;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.BasicWebServerIocModule;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.mcp.ioc.McpIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test.ioc.PlatformTestServerIocModule;

import java.util.List;
import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;

public final class TgMcpServerTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {

    private final Injector injector;

    public TgMcpServerTestCaseConfiguration(final Properties properties) {
        final var appProperties = getProperties(properties);
        final var appDomain = new PlatformTestDomainTypes();
        injector = new ApplicationInjectorFactory()
                .add(new IocModule(appDomain, appDomain.entityTypes(), appProperties))
                .add(new McpIocModule())
                .add(new NewUserEmailNotifierTestIocModule())
                .getInjector();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
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
        props.setProperty(AUDIT_MODE, AuditingMode.DISABLED.name());
        props.setProperty("attachments.location", "src/test/resources/attachments");
        props.setProperty("attachments.allowlist",
                          "text/plain,application/pdf,application/zip,application/x-zip-compressed,application/gzip," +
                          "application/x-tar,application/x-gtar,application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        props.setProperty("email.smtp", "non-existing-server");
        props.setProperty("email.fromAddress", "platform@fielden.com.au");
        props.setProperty("web.api", "true");
        props.setProperty("web.domain", "tgdev.com");
        props.setProperty("web.port", "443");
        props.setProperty("port", "8091");
        props.setProperty("web.path", "/");
        return props;
    }

    private static class IocModule extends PlatformTestServerIocModule {

        public IocModule(
                final IApplicationDomainProvider applicationDomainProvider,
                final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
                final Properties props)
        {
            super(applicationDomainProvider, domainEntityTypes, props);
        }

    }

}
