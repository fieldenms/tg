package ua.com.fielden.platform.web.test.server;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.web.vulcanizer.VulcanizingUtility;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.web.test.server.TgTestApplicationProperties.defaultConfigFileName;
import static ua.com.fielden.platform.web.test.server.TgTestApplicationProperties.putDatabaseConnectionPropertiesIfAbsent;

/// Web UI vulcanization launching class for TG example web server.
///
public class Vulcanize extends VulcanizingUtility {
    
    /// The procedure of vulcanization all-in-one.
    ///
    /// @param args an optional application properties file (`*.properties`), followed by additional paths for the `PATH` environment variable and environment variables as `NAME=VALUE`.
    ///             Without a properties file, it is determined by `databaseUri`, the same as for `Start` and `PopulateDb`.
    /// @throws IOException
    public static void main(final String[] args) throws IOException {
        final var effectiveArgs = args.length > 0 && args[0].endsWith(".properties") ? args : ArrayUtils.addFirst(args, defaultConfigFileName());
        final T3<Properties, String[], String[]> propsAndAdditionalPaths = processVmArguments(effectiveArgs);
        // Database connection properties absent in the application properties file come from system properties, the same as for `Start` and `PopulateDb`.
        putDatabaseConnectionPropertiesIfAbsent(propsAndAdditionalPaths._1);
        final Logger logger = getLogger(Vulcanize.class);
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