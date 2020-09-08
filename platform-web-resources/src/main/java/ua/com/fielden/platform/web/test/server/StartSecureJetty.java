package ua.com.fielden.platform.web.test.server;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Web UI Testing Server launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class StartSecureJetty {
    private static final Logger logger = getLogger(StartSecureJetty.class);

    public static void main(final String[] args) throws Exception {
        final String fileName = "src/main/resources/application.properties";

        final Properties props = new Properties();
        try (final InputStream st = new FileInputStream(fileName)) {
            props.load(st);
        }

        //DOMConfigurator.configure(props.getProperty("log4j"));

        logger.info("Starting...");

        final Server server = new Server();

        try (final ServerConnector connector = new ServerConnector(server)) {
            connector.setHost("localhost");

            final int port = 9998;

            final HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());
            https.setSecurePort(port);
            https.setSecureScheme("https");

            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(ResourceLoader.getURL("tsl/ca-signed-keystore").getPath());
            sslContextFactory.setKeyStorePassword("changeit");
            sslContextFactory.setKeyManagerPassword("changeit");

            final ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector.setPort(port);

            server.setConnectors(new Connector[] { connector, sslConnector });

            server.start();
            logger.info("Started");
        }
    }
}