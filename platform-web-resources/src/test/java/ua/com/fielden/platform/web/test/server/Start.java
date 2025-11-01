package ua.com.fielden.platform.web.test.server;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;

import org.restlet.util.Series;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

/**
 * Web UI Testing Server launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class Start {
    private static final Logger LOGGER = getLogger(Start.class);

    public static void main(final String[] args) throws IOException {
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

        LOGGER.info("Starting...");
        final Component component = new TgTestApplicationConfiguration(props);
        component.getServers().add(Protocol.HTTP, Integer.parseInt(props.getProperty("port")));
        // Jetty needs additional settings to react to a shutdown signal, sent to JVM.
        final var server = component.getServers().getFirst();
        final Series<Parameter> parameters = server.getContext().getParameters();
        // Parameters to ensure quick shutdown for the test app instead of waiting for the default 30 seconds.
        parameters.add("shutdown.timeout", "1");
        parameters.add("shutdown.gracefully", "true");


        try {
            component.start();
            LOGGER.info("started");
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}
