package ua.com.fielden.platform.test;

import com.google.inject.Injector;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.ioc.PlatformTestServerIocModule;

import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_PATH;

/// Provides Platform specific implementation of [IDomainDrivenTestCaseConfiguration] for testing purposes,
/// which is mainly related to construction of appropriate IoC modules.
///
public final class PlatformDomainDrivenTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {

    private final Injector injector;

    public PlatformDomainDrivenTestCaseConfiguration(final Properties properties) {
        try {
            final var appDomainProvider = new PlatformTestDomainTypes();
            injector = new ApplicationInjectorFactory()
                    .add(new PlatformTestServerIocModule(
                            appDomainProvider,
                            appDomainProvider.entityTypes(),
                            getProperties(properties)))
                    .add(new NewUserEmailNotifierTestIocModule())
                    .getInjector();

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties getProperties(final Properties properties) {
        final Properties props = new Properties(properties);
        // application properties
        props.setProperty("workflow", "development");
        props.setProperty("app.name", "TG Test");
        props.setProperty("reports.path", "");
        props.setProperty("domain.path", "../platform-pojo-bl/target/classes");
        props.setProperty("domain.package", "ua.com.fielden.platform");
        props.setProperty("tokens.path", "../platform-pojo-bl/target/classes");
        props.setProperty("tokens.package", "ua.com.fielden.platform.security.tokens");
        props.setProperty(AUDIT_PATH, "../platform-pojo-bl/target/classes");
        props.setProperty(AUDIT_MODE, AuditingMode.ENABLED.name());
        props.setProperty("attachments.location", "src/test/resources/attachments");
        props.setProperty("attachments.allowlist", "text/plain,application/pdf,application/zip,application/x-zip-compressed,application/gzip,application/x-tar,application/x-gtar,application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        props.setProperty("email.smtp", "non-existing-server");
        props.setProperty("email.fromAddress", "platform@fielden.com.au");
        props.setProperty("web.api", "true");
        props.setProperty("web.domain", "tgdev.com");
        props.setProperty("web.port", "443");
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

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

}
