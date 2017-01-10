package ua.com.fielden.platform.web.test.server;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.VulcanizingUtility;

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
        final Pair<Properties, String[]> propsAndAdditionalPaths = processVmArguments(args);
        final Logger logger = Logger.getLogger(Vulcanize.class);
        logger.info("Starting app...");
        final TgTestApplicationConfiguration component = new TgTestApplicationConfiguration(propsAndAdditionalPaths.getKey());
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
                VulcanizingUtility::windowsCommands, 
                propsAndAdditionalPaths.getValue());
    }

}