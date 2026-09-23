package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.mcp.ioc.McpIocModule;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test.ioc.PlatformTestServerIocModule;

import java.util.List;
import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;

public final class TgMcpServerTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {

    public static final String USER_MCP_TEST = "MCP_TEST";
    public static final String PATH_MCP = "/mcp";
    public static final String TEST_WEB_API_KEY_MCP = "secret";

    private final Injector injector;

    public TgMcpServerTestCaseConfiguration(final Properties properties) {
        final var appProperties = getProperties(properties);
        final var appDomain = new PlatformTestDomainTypes();
        injector = new ApplicationInjectorFactory()
                .add(IocModule.create(appDomain, appDomain.entityTypes(), appProperties))
                .add(new McpIocModule(appProperties))
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
        // MCP
        props.setProperty(McpConfig.MCP_RESOURCE_PATH, PATH_MCP);
        props.setProperty(McpConfig.WEB_API_KEY_MCP, TEST_WEB_API_KEY_MCP);
        props.setProperty(McpConfig.MCP_USER, USER_MCP_TEST);
        return props;
    }

    private static class IocModule extends PlatformTestServerIocModule {

        public static Module create(
                final IApplicationDomainProvider applicationDomainProvider,
                final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
                final Properties props)
        {
            return Modules.override(new IocModule(applicationDomainProvider, domainEntityTypes, props))
                    .with(moduleWithOverrides());
        }

        private IocModule(
                final IApplicationDomainProvider applicationDomainProvider,
                final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
                final Properties props)
        {
            super(applicationDomainProvider, domainEntityTypes, props);
        }

        private static Module moduleWithOverrides() {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    // Use SharedUserProvider so that the user set in test methods is visible to all threads (Restlet,
                    // Reactor, etc.).
                    bind(IUserProvider.class).to(SharedUserProvider.class);
                    bind(IAuthorisationModel.class).to(AuthorisationModelForTests.class);
                }
            };
        }

    }

}
