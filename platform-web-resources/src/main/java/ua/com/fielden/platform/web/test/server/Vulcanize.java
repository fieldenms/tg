package ua.com.fielden.platform.web.test.server;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.web.utils.VulcanizingUtility;

/**
 * Web UI vulcanization launching class for TG example web server.
 *
 * @author TG Team
 *
 */
public class Vulcanize extends VulcanizingUtility {
    
    public static String[] unixCommands(final String prefix) {
        return new String[] {"/bin/bash", prefix + "-script.sh"};
    }
    
    public static String[] windowsCommands4Jhou(final String prefix) {
        return new String[] {"C:/Users/Yuriy/AppData/Roaming/npm/vulcanize.cmd", "-p", "\"vulcan/\"", "/" + prefix + "-startup-resources-origin.html", ">", prefix + "-startup-resources-origin-vulcanized.html"};
    }

    /**
     * The procedure of vulcanization all-in-one.
     *
     * @param args
     */
    public static void main(final String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(""
                    + "One or two arguments are expected: \n"
                    + "\t1st is the path to the application properties file;\n"
                    + "\t2nd is the additional paths to be added to the PATH env. variable.\n");
        }
        
        final Logger logger = Logger.getLogger(Vulcanize.class);
        if (args.length > 2) {
            logger.warn("There are more than 2 arguments. Only first two will be used, the rest will be ignored.");
        }
        final String propertyFile;
        final String paths;
        if (args.length == 1) {
            propertyFile = args[0];
            paths = "";
        } else {
            propertyFile = args[0];
            paths = args[1];
        }
        
        final Properties props = retrieveApplicationPropertiesAndConfigureLogging(propertyFile);
        final String[] additionalPaths = paths.split(File.pathSeparator);

        logger.info("Starting app...");
        final TgTestApplicationConfiguration component = new TgTestApplicationConfiguration(props);
        logger.info("Started app.");

        final String platformVendorResourcesPath = "../platform-web-ui/src/main/resources";
        final String platformWebUiResourcesPath = "../platform-web-ui/src/main/web/ua/com/fielden/platform/web";
        final String appVendorResourcesPath = null;
        final String appWebUiResourcesPath = null;
        final String loginTargetPlatformSpecificPath = "../platform-web-ui/src/main/web/ua/com/fielden/platform/web/";
        final String mobileAndDesktopAppSpecificPath = "../platform-web-ui/src/main/web/ua/com/fielden/platform/web/";

        vulcanize(
                component.injector(), 
                platformVendorResourcesPath, 
                platformWebUiResourcesPath, 
                appVendorResourcesPath, 
                appWebUiResourcesPath, 
                loginTargetPlatformSpecificPath, 
                mobileAndDesktopAppSpecificPath, 
                Vulcanize::unixCommands, 
                additionalPaths,
                logger);
    }

}