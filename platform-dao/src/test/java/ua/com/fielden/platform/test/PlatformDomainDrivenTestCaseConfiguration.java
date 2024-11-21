package ua.com.fielden.platform.test;

import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.audit.Audited;
import ua.com.fielden.platform.audit.DynamicAuditEntityGenerator;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.BasicWebServerIocModule;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.test.ioc.PlatformTestServerIocModule;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.test.ioc.AuditingTestIocModule.GENERATED_AUDIT_SOURCES_PATH;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/**
 * Provides Platform specific implementation of {@link IDomainDrivenTestCaseConfiguration} for testing purposes, which is mainly related to construction of appropriate IoC modules.
 *
 * <h4> Dynamic generation of audit-entity types </h4>
 * This test case configuration enables the use of dynamically generated audit-entity types.
 * The following steps are taken to achieve this:
 * <ol>
 *   <li> Create a temporary application to generate, compile, and load the bytecode for audit-entity types;
 *        afterwards, this temporary application is discarded.
 *   <li> Creates the primary application that will be able to discover the generated audit-entity types.
 * </ol>
 * <p>
 * For each registered entity type annotated with {@link Audited}, audit-entity types will be generated dynamically before tests are run.
 *
 * @author TG Team
 * 
 */
public final class PlatformDomainDrivenTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Injector injector;

    public PlatformDomainDrivenTestCaseConfiguration(final Properties properties) {
        try {
            final var appDomainProvider = new PlatformTestDomainTypes();

            generateAuditTypes(properties, appDomainProvider);

            injector = new ApplicationInjectorFactory()
                    .add(new PlatformTestServerIocModule(
                            appDomainProvider,
                            appDomainProvider.entityTypes(),
                            getProperties(properties)))
                    .add(new NewUserEmailNotifierTestIocModule())
                    .getInjector();

        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Collection<Class<?>> generateAuditTypes(
            final Properties properties,
            final IApplicationDomainProvider appDomainProvider)
    {
        final var auditedEntityTypes = appDomainProvider.entityTypes().stream()
                .filter(AuditUtils::isAudited)
                .collect(toSet());

        if (!auditedEntityTypes.isEmpty()) {
            // A temporary injector to access the audit-entity generator
            final var tmpInjector = new ApplicationInjectorFactory()
                    .add(new PlatformTestServerIocModule(
                            appDomainProvider,
                            appDomainProvider.entityTypes(),
                            // Enable audit-entity generation mode
                            getProperties(properties, t2(BasicWebServerIocModule.GEN_AUDIT_MODE, ""))))
                    .add(new NewUserEmailNotifierTestIocModule())
                    .getInjector();
            // Dynamically generate audit-entity types, which will result in them being loaded by the system class loader
            final var generator = tmpInjector.getInstance(DynamicAuditEntityGenerator.class);
            final var auditTypes = auditedEntityTypes.stream()
                    .map(generator::generateAuditTypes)
                    .flatMap(Collection::stream)
                    .toList();

            LOGGER.info(format("Generated and loaded %s audit types: [%s]",
                               auditTypes.size(),
                               CollectionUtil.toString(auditTypes, Class::getSimpleName, ", ")));
            return auditTypes;
        }
        else {
            return List.of();
        }
    }

    @SafeVarargs
    private static Properties getProperties(final Properties hbc, final T2<String, String>... properties) {
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
        props.setProperty("email.smtp", "non-existing-server");
        props.setProperty("email.fromAddress", "platform@fielden.com.au");
        props.setProperty("web.api", "true");
        // Custom Hibernate configuration properties
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");

        props.setProperty(GENERATED_AUDIT_SOURCES_PATH, "src/test/resources/generated-audit-sources");

        for (final var pair : properties) {
            props.setProperty(pair._1, pair._2);
        }

        return props;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

}
