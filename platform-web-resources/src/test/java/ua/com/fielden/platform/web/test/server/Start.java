package ua.com.fielden.platform.web.test.server;

import org.apache.logging.log4j.Logger;
import org.restlet.Component;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.utils.MiscUtilities;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T2.toMap;
import static ua.com.fielden.platform.utils.MiscUtilities.propertiesUnionLeft;
import static ua.com.fielden.platform.utils.MiscUtilities.readProperties;

/// Launches the test application.
///
public class Start {

    private static final Logger LOGGER = getLogger();

    public static void main(final String[] args) {

        final String configFilePath;
        if (args.length >= 1) {
            configFilePath = args[0];
        }
        else {
            final var databaseUri = System.getProperty("databaseUri");
            if (isEmpty(databaseUri)) {
                throw new ApplicationConfigurationException("System property [databaseUri] is required.");
            }
            else {
                configFilePath = databaseUri.contains("5432")
                        ? "src/main/resources/application-PostgreSql.properties"
                        : "src/main/resources/application-SqlServer.properties";
            }
        }

        LOGGER.info("Application properties file: %s".formatted(configFilePath));

        final var requiredPropertyNames = List.of("databaseUri", "databaseUser", "databasePasswd", "port");

        // Properties from the file take precedence over System properties.
        final var properties = propertiesUnionLeft(readProperties(configFilePath),
                                                   requiredPropertyNames.stream()
                                                           .map(prop -> Optional.ofNullable(System.getProperty(prop)).map(v -> t2(prop, v)))
                                                           .flatMap(Optional::stream)
                                                           .collect(collectingAndThen(toMap(), MiscUtilities::mkProperties)));

        final var missingPropertyNames = requiredPropertyNames.stream().filter(prop -> !properties.containsKey(prop)).toList();
        if (!missingPropertyNames.isEmpty()) {
            throw new ApplicationConfigurationException("Required application properties are missing: %s".formatted(String.join(", ", missingPropertyNames)));
        }

        properties.put("hibernate.connection.url",
                       Optional.of(properties.getProperty("databaseUri"))
                               .map(uri -> uri.contains("5432") ? "jdbc:postgresql:" + uri : "jdbc:sqlserver:" + uri)
                               .orElseThrow());
        properties.put("hibernate.connection.username", properties.get("databaseUser"));
        properties.put("hibernate.connection.password", properties.get("databasePasswd"));

        LOGGER.info("Starting the web server...");
        final Component component = new TgTestApplicationConfiguration(properties);
        component.getServers().add(Protocol.HTTP, Integer.parseInt(properties.getProperty("port")));
        // Jetty needs additional settings to react to a shutdown signal, sent to JVM.
        final var server = component.getServers().getFirst();
        final Series<Parameter> parameters = server.getContext().getParameters();
        // Parameters to ensure quick shutdown for the test app instead of waiting for the default 30 seconds.
        parameters.add("shutdown.timeout", "1");
        parameters.add("shutdown.gracefully", "true");

        try {
            component.start();
            LOGGER.info("Started the web server.");
        } catch (final Exception ex) {
            LOGGER.error("An error occurred, exiting.", ex);
            System.exit(100);
        }
    }

}
