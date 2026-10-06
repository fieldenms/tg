package ua.com.fielden.platform.web.test.server;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

/// Properties of the TG test application, shared by its launchers [Start], [PopulateDb] and [Vulcanize].
///
/// Required system properties `databaseUri`, `databaseUser` and `databasePasswd` specify the database connection.
/// A `databaseUri` with port `5432` designates PostgreSQL, and any other `databaseUri` designates SQL Server.
///
final class TgTestApplicationProperties {

    private TgTestApplicationProperties() {}

    /// Loads the TG test application properties.
    /// Database connection properties are created from system properties, as per [#databaseConnectionProperties()].
    /// The other properties are loaded from the file specified as the only element of `args`.
    /// Without such a file, [#defaultConfigFileName()] is loaded.
    /// Database connection properties in the loaded file take precedence over those created from system properties.
    ///
    static Properties load(final String[] args) throws IOException {
        final var props = databaseConnectionProperties();
        try (final var in = new FileInputStream(args.length == 1 ? args[0] : defaultConfigFileName())) {
            props.load(in);
        }
        return props;
    }

    /// Creates database connection properties `hibernate.connection.*` from system properties.
    ///
    static Properties databaseConnectionProperties() {
        final var props = new Properties();
        final var jdbcPrefix = isPostgreSql() ? "jdbc:postgresql:" : "jdbc:sqlserver:";
        props.put("hibernate.connection.url", jdbcPrefix + requiredSystemProperty("databaseUri"));
        props.put("hibernate.connection.username", requiredSystemProperty("databaseUser"));
        props.put("hibernate.connection.password", requiredSystemProperty("databasePasswd"));
        return props;
    }

    /// Returns the path to `application-PostgreSql.properties` or `application-SqlServer.properties`.
    /// The choice depends on `databaseUri`.
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
