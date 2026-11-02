package ua.com.fielden.platform.web.test.server;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

/// Properties of the TG test application, shared by its launchers [Start], [PopulateDb] and [Vulcanize].
///
/// System properties `databaseUri`, `databaseUser` and `databasePasswd` specify the database connection, unless the application properties file does.
/// A `databaseUri` with port `5432` designates PostgreSQL, and any other `databaseUri` designates SQL Server.
///
final class TgTestApplicationProperties {

    private TgTestApplicationProperties() {}

    /// Loads the TG test application properties from the file specified as the only element of `args`.
    /// Without such a file, [#defaultConfigFileName()] is loaded.
    /// Database connection properties absent in the loaded file are created from system properties, as per [#putDatabaseConnectionPropertiesIfAbsent(Properties)].
    ///
    static Properties load(final String[] args) throws IOException {
        final var props = new Properties();
        try (final var in = new FileInputStream(args.length == 1 ? args[0] : defaultConfigFileName())) {
            props.load(in);
        }
        putDatabaseConnectionPropertiesIfAbsent(props);
        return props;
    }

    /// Puts into `props` database connection properties `hibernate.connection.*` that are absent there, creating them from system properties.
    /// A system property is required only if the connection property created from it is absent.
    ///
    static void putDatabaseConnectionPropertiesIfAbsent(final Properties props) {
        props.computeIfAbsent("hibernate.connection.url", _ -> (isPostgreSql() ? "jdbc:postgresql:" : "jdbc:sqlserver:") + requiredSystemProperty("databaseUri"));
        props.computeIfAbsent("hibernate.connection.username", _ -> requiredSystemProperty("databaseUser"));
        props.computeIfAbsent("hibernate.connection.password", _ -> requiredSystemProperty("databasePasswd"));
    }

    /// Returns the path to `application-PostgreSql.properties` or `application-SqlServer.properties`.
    /// The choice depends on `databaseUri`, which is therefore required.
    /// The path is relative to the `platform-web-resources` module.
    ///
    static String defaultConfigFileName() {
        return "src/main/resources/application-%s.properties".formatted(isPostgreSql() ? "PostgreSql" : "SqlServer");
    }

    private static boolean isPostgreSql() {
        return requiredSystemProperty("databaseUri").contains("5432");
    }

    private static String requiredSystemProperty(final String name) {
        final var value = getProperty(name);
        if (isEmpty(value)) {
            throw new ApplicationConfigurationException("Property '%s' is required.".formatted(name));
        }
        return value;
    }

}
