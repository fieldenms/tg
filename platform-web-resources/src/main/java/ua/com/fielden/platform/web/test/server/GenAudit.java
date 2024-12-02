package ua.com.fielden.platform.web.test.server;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.AuditEntityGenerator;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.ioc.BasicWebServerIocModule.GEN_AUDIT_MODE;

/**
 * This is a helper class, which generates audit-entities.
 *
 * @author TG Team
 */
public class GenAudit {
    private static final Logger LOGGER = getLogger();

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Initialising...");
        final var props = new Properties();
        final String propsFileSuffix; // is used to load either application-PostreSql.properties or application-SqlServer.properties
        // Three system properties are required: databaseUri, databaseUser and databasePasswd.
        final var databseUri = System.getProperty("databaseUri");
        if (isEmpty(databseUri)) {
            throw new ApplicationConfigurationException("Property 'databaseUri' is required.");
        } else {
            final String jdbcUri;
            if (databseUri.contains("5432")) {
                propsFileSuffix = "PostgreSql";
                jdbcUri = "jdbc:postgresql:" + databseUri; 
            } else {
                propsFileSuffix = "SqlServer";
                jdbcUri = "jdbc:sqlserver:" + databseUri; 
 
            }
            props.put("hibernate.connection.url", jdbcUri);
        }
        final var dbUser = System.getProperty("databaseUser");
        if (isEmpty(dbUser)) {
            throw new ApplicationConfigurationException("Property 'databaseUser' is required.");
        } else {
            props.put("hibernate.connection.username", dbUser);
        }
        final var dbPasswd = System.getProperty("databasePasswd");
        if (isEmpty(dbPasswd)) {
            throw new ApplicationConfigurationException("Property 'databasePasswd' is required.");
        } else {
            props.put("hibernate.connection.password", dbPasswd);
        }
        // Default application-PostreSql.properties and application-SqlServer.properties do not have any of the properties already assigned from system properties databaseUri, databaseUser and databasePasswd.
        // However, if some alternative application.properties is provided, which contains those properties, the values from the file will get used.
        final String configFileName = args.length == 1 ? args[0] : "src/main/resources/application-%s.properties".formatted(propsFileSuffix);
        try (final FileInputStream in = new FileInputStream(configFileName)) {
            props.load(in);
        }

        final var config = new AuditGenerationConfig(props);
        LOGGER.info("Generating audit types...");

        final var generator = config.getInstance(AuditEntityGenerator.class);
        final var results = generator.generate(
                // Specify audited entity types
                List.of(),
                Path.of("../platform-pojo-bl/src/audit/java"));
        results.forEach(r -> {
            LOGGER.info("Generated audit-entity %s".formatted(r.auditEntityPath()));
            LOGGER.info("Generated audit-prop entity %s".formatted(r.auditPropEntityPath()));
        });
    }

}
