package ua.com.fielden.platform.web.test.server;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.web.test.server.TgTestApplicationProperties.load;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;

import org.restlet.util.Series;

/**
 * Web UI Testing Server launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class Start {
    private static final Logger LOGGER = getLogger(Start.class);

    public static void main(final String[] args) throws IOException {
        final var props = load(args);

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
