package ua.com.fielden.platform.web.test.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Web UI vulcanization launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class Unvulcanize {
    private static final Logger logger = Logger.getLogger(Unvulcanize.class);

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

        unvulcanize();
    }

    /**
     * Unvulcanizes 'startup-resources.html' file (make it to be the same as 'startup-resources-origin.html').
     */
    private static void unvulcanize() {
        logger.info("Unvulcanizing...");

        logger.info("\tCopying [startup-resources-origin] into [startup-resources]...");
        new File("vulcan/resources").mkdir();
        try {
            FileUtils.copyFile(new File("../platform-web-ui/src/main/web/ua/com/fielden/platform/web/startup-resources-origin.html"), new File("../platform-web-ui/src/main/web/ua/com/fielden/platform/web/startup-resources.html"));
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tCopied [startup-resources-origin] into [startup-resources].");

        logger.info("Unvulcanized.");
    }
}