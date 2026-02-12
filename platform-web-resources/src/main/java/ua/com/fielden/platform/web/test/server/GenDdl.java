package ua.com.fielden.platform.web.test.server;

import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.ddl.IDdlGenerator;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.io.FileInputStream;
import java.util.Properties;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_PATH;
import static ua.com.fielden.platform.ioc.exceptions.MissingParameterDependencyException.requireNonEmpty;

/// This is a helper class, which generates DDL script for one or several entities.
///
public class GenDdl {
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

        // Enable auditing to be able to generate DDL for audit types.
        props.setProperty(AUDIT_PATH, "../platform-pojo-bl/target/classes");
        props.setProperty(AUDIT_MODE, AuditingMode.ENABLED.name());

        LOGGER.info("Obtaining Hibernate dialect ...");
        final Class<?> dialectType = Class.forName(props.getProperty("hibernate.dialect"));
        final Dialect dialect = (Dialect) dialectType.getDeclaredConstructor().newInstance();
        LOGGER.info("Running with dialect %s ...".formatted(dialect));
        final DataPopulationConfig config = new DataPopulationConfig(props);
        LOGGER.info("Generating DDL and running it against the target DB ...");

        final var ddlGenerator = config.getInstance(IDdlGenerator.class);

        // Generate DDL for ordinary entity types.

        ddlGenerator.generateDatabaseDdl(dialect, TgVehicle.class).forEach(System.out::println);


        // Generate DDL for audit types.

        // final var auditTypeFinder = config.getInstance(IAuditTypeFinder.class);
        //
        // auditTypeFinder.navigate(TgVehicle.class).allPersistentAuditTypes().forEach(auditType -> {
        //     System.out.println();
        //     ddlGenerator.generateDatabaseDdl(dialect, auditType).forEach(System.out::println);
        //     System.out.println();
        // });

    }

}
