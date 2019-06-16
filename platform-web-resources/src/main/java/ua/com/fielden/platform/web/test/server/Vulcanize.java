package ua.com.fielden.platform.web.test.server;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.web.vulcanizer.VulcanizingUtility;

/**
 * Web UI vulcanization launching class for TG example web server.
 *
 * @author TG Team
 *
 */
public class Vulcanize extends VulcanizingUtility {
    
    /**
     * The procedure of vulcanization all-in-one.
     *
     * @param args
     * @throws IOException 
     */
    public static void main(final String[] args) throws IOException {
        final T3<Properties, String[], String[]> propsAndAdditionalPaths = processVmArguments(args);
        final Logger logger = Logger.getLogger(Vulcanize.class);
        logger.info("Starting app...");
        final TgTestApplicationConfiguration component = new TgTestApplicationConfiguration(propsAndAdditionalPaths._1);
        logger.info("Started app.");

        final String platformVendorResourcesPath = "../platform-web-ui/src/main/resources";
        final String platformWebUiResourcesPath = "../platform-web-ui/src/main/web/ua/com/fielden/platform/web";
        final String appVendorResourcesPath = null;
        final String appWebUiResourcesPath = null;
        final String loginTargetPlatformSpecificPath = "../platform-web-ui/src/main/web/ua/com/fielden/platform/web/";
        final String mobileAndDesktopAppSpecificPath = "../platform-web-ui/src/main/web/ua/com/fielden/platform/web/";

        final Function<String, String[]> commandMaker = System.getProperty("os.name").toLowerCase().contains("windows") ? Vulcanize::windowsCommands : Vulcanize::unixCommands;
        vulcanize(
                component.injector(), 
                platformVendorResourcesPath, 
                platformWebUiResourcesPath, 
                appVendorResourcesPath, 
                appWebUiResourcesPath, 
                loginTargetPlatformSpecificPath, 
                mobileAndDesktopAppSpecificPath, 
                commandMaker, 
                propsAndAdditionalPaths._2,
                propsAndAdditionalPaths._3);
    }

}