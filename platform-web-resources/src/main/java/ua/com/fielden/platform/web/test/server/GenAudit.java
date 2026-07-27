package ua.com.fielden.platform.web.test.server;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.IAuditEntityGenerator;
import ua.com.fielden.platform.audit.IAuditEntityGenerator.VersionStrategy;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.ioc.exceptions.MissingParameterDependencyException.requireNonEmpty;

/// This is a helper class, which generates audit-entities.
///
public class GenAudit {
    private static final Logger LOGGER = getLogger();

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Initialising...");
        final var props = new Properties();
        final String propsFileSuffix; // is used to load either application-PostreSql.properties or application-SqlServer.properties

        // Three system properties are required: databaseUri, databaseUser and databasePasswd.

        final var databaseUri = requireNonEmpty(System.getProperties(), "databaseUri");
        final String jdbcUri;
        if (databaseUri.contains("5432")) {
            propsFileSuffix = "PostgreSql";
            jdbcUri = "jdbc:postgresql:" + databaseUri;
        } else {
            propsFileSuffix = "SqlServer";
            jdbcUri = "jdbc:sqlserver:" + databaseUri;
        }
        props.put("hibernate.connection.url", jdbcUri);

        final var dbUser = requireNonEmpty(System.getProperties(), "databaseUser");
        props.put("hibernate.connection.username", dbUser);

        final var dbPasswd = requireNonEmpty(System.getProperties(), "databasePasswd");
        props.put("hibernate.connection.password", dbPasswd);

        // Default application-PostreSql.properties and application-SqlServer.properties do not have any of the properties already assigned from system properties databaseUri, databaseUser and databasePasswd.
        // However, if some alternative application.properties is provided, which contains those properties, the values from the file will get used.
        final String configFileName = args.length == 1 ? args[0] : "src/main/resources/application-%s.properties".formatted(propsFileSuffix);
        try (final FileInputStream in = new FileInputStream(configFileName)) {
            props.load(in);
        }

        final var config = new AuditGenerationConfig(props);
        LOGGER.info("Generating audit types...");

        final var generator = config.getInstance(IAuditEntityGenerator.class);
        final var results = generator.generate(
                // Specify audited entity types
                List.of(TgVehicle.class),
                Path.of("../platform-pojo-bl/src/audit/java"),
                VersionStrategy.NEW);
        LOGGER.info(() -> "Generated audit types:\n%s".formatted(results.stream().map(p -> p.toAbsolutePath().normalize().toString()).collect(joining("\n"))));
    }

}
