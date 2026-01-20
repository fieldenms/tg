package ua.com.fielden.platform.web.resources.test;

import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.IModuleWithInjector;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.ioc.IBasicWebApplicationServerModule;

import java.util.Properties;

public final class WebResourcesTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {

    private final Injector injector;

    public WebResourcesTestCaseConfiguration(final Properties properties) {
        final var appDomainProvider = new WebResourcesTestDomainTypes();
        final var appProperties = getProperties(properties);
        injector = new ApplicationInjectorFactory()
                .add(new WebResourcesTestIocModule(appDomainProvider, appDomainProvider.entityTypes(), appProperties))
                .add(new WebIocModule(appProperties))
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
        // Custom Hibernate configuration properties
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");
        // Cache configuration for the dynamic property access
        props.setProperty("dynamicPropertyAccess.caching", "enabled");
        props.setProperty("dynamicPropertyAccess.typeCache.concurrencyLevel", "100");
        props.setProperty("dynamicPropertyAccess.typeCache.expireAfterAccess", "12h");
        props.setProperty("dynamicPropertyAccess.tempTypeCache.maxSize", "2048");
        props.setProperty("dynamicPropertyAccess.tempTypeCache.expireAfterWrite", "10m");
        return props;
    }

    private static class WebIocModule extends AbstractPlatformIocModule implements IBasicWebApplicationServerModule, IModuleWithInjector {

        private final Properties appProperties;

        public WebIocModule(final Properties appProperties) {
            this.appProperties = appProperties;
        }

        @Override
        protected void configure() {
            final var webUiConfig = new WebUiConfig(appProperties);
            bind(IWebUiConfig.class).toInstance(webUiConfig);
            bindWebAppResources(webUiConfig);
        }

        @Override
        public void setInjector(final Injector injector) {
            initWebApp(injector);
        }

        @Override
        public <T> AnnotatedBindingBuilder<T> bindType(final Class<T> type) {
            return bind(type);
        }

    }

}
