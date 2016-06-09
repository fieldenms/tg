package ua.com.fielden.platform.web.test.server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.restlet.Component;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Web UI Testing Server launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class StartSecure {
    private static final Logger logger = Logger.getLogger(StartSecure.class);

    public static void main(final String[] args) {
        final String fileName = "src/main/resources/application.properties";
        InputStream st = null;
        Properties props = null;
        try {
            st = new FileInputStream(fileName);
            props = new Properties();
            props.load(st);
        } catch (final Exception e) {
            System.out.println(String.format("Application property file %s could not be located or its values are not recognised.", fileName));
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                st.close();
            } catch (final Exception e) {
                e.printStackTrace(); // can be ignored
            }
        }

        DOMConfigurator.configure(props.getProperty("log4j"));

        logger.info("Starting...");
        final Component component = new TgTestApplicationConfiguration(props);
        //component.getServers().add(Protocol.HTTP, Integer.parseInt(props.getProperty("port")));

        final org.restlet.Server server = component.getServers().add(Protocol.HTTPS, Integer.parseInt(props.getProperty("port")));
        final Series<Parameter> parameters = server.getContext().getParameters();

        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
        // I have created self signed certificate. reference is attached with parameter
        parameters.add("keystorePath", ResourceLoader.getURL("tls/ca-signed-keystore").getPath());

        parameters.add("keystorePassword", "changeit");
        parameters.add("keyPassword", "changeit");
        parameters.add("keystoreType", "JKS");

        try {
            component.start();
            logger.info("started");
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}