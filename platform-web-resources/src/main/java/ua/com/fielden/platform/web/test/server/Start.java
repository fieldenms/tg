package ua.com.fielden.platform.web.test.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Web UI Testing Server launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class Start {
    private static final Logger LOGGER = Logger.getLogger(Start.class);

    public static void main(final String[] args) throws IOException {
        final String fileName = "src/main/resources/application.properties";
        final Properties props = new Properties();
        try (final InputStream st = new FileInputStream(fileName);) {
            props.load(st);
        }

        DOMConfigurator.configure(props.getProperty("log4j"));

        LOGGER.info("Starting...");
        final Component component = new TgTestApplicationConfiguration(props);
        component.getServers().add(Protocol.HTTP, Integer.parseInt(props.getProperty("port")));

        try {
            component.start();
            LOGGER.info("started");
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}