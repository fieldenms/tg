package ua.com.fielden.platform.web.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.google.common.base.Charsets;
import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/**
 * A set of utilities to facilitate Web UI application vulcanization.
 *
 * @author TG Team
 *
 */
public class VulcanizingUtility {
    private static final Logger LOGGER = Logger.getLogger(VulcanizingUtility.class);

    public static String[] unixCommands(final String prefix) {
        return new String[] {"/bin/bash", prefix + "-script.sh"};
    }

    public static String[] windowsCommands(final String prefix) {
        // JVM arguments (brackets should be removed): [src/main/resources/application.properties "C:/Program Files/nodejs;C:/Users/Yuriy/AppData/Roaming/npm"]
        return new String[] {"CMD", "/c", "vulcanize", "--strip-comments", "-p", "\"vulcan/\"", "/" + prefix + "-startup-resources-origin.html", "^>", prefix + "-startup-resources-origin-vulcanized.html"};
        // OTHER WAY: create three files login-script.bat, desktop-script.bat and mobile-script.bat and place them where similar *.sh scripts reside.
        // Contents of the login-script.bat file should be following (brackets should be removed): [vulcanize -p "vulcan/" /login-startup-resources-origin.html ^> login-startup-resources-origin-vulcanized.html].
        // UNCOMMENT: return new String[] {"CMD", "/c", prefix + "-script.bat"};
    }

    protected static Pair<Properties, String[]> processVmArguments(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException(""
                    + "One or two arguments are expected: \n"
                    + "\t1st is the path to the application properties file;\n"
                    + "\t2nd is the additional paths to be added to the PATH env. variable.\n");
        }

        if (args.length > 2) {
            LOGGER.warn("There are more than 2 arguments. Only first two will be used, the rest will be ignored.");
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

        try {
            final Properties props = retrieveApplicationPropertiesAndConfigureLogging(propertyFile);
            final String[] additionalPaths = paths.split(File.pathSeparator);
            return Pair.pair(props, additionalPaths);
        } catch (final IOException ex) {
            LOGGER.fatal(String.format("Application property file %s could not be located or its values are not recognised.", propertyFile), ex);
            throw ex;
        }

    }

    /**
     * Retrieves application properties from the specified file.
     *
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static Properties retrieveApplicationPropertiesAndConfigureLogging(final String fileName) throws IOException {
        final Properties props = new Properties();
        try (final InputStream st = new FileInputStream(fileName)){
            props.load(st);
        }

        // needs to be overridden to start vulcanization in development mode (no need to calculate preloaded resources)
        props.setProperty("workflow", "vulcanizing");

        // configure logging
        DOMConfigurator.configure(props.getProperty("log4j"));

        return props;
    }

    /**
     * Vulcanizes '*-startup-resources-origin.html' file into '*-startup-resources-vulcanized.html'.
     *
     * @param injector
     * @param platformVendorResourcesPath
     * @param platformWebUiResourcesPath
     * @param appVendorResourcesPath
     * @param appWebUiResourcesPath
     * @param loginTargetPlatformSpecificPath
     * @param mobileAndDesktopAppSpecificPath
     * @param commandMaker -- a function that produces a sequence of string values to build a vulcanizer process
     * @param logger
     */
    public static void vulcanize(
            final Injector injector,
            final String platformVendorResourcesPath,
            final String platformWebUiResourcesPath,
            final String appVendorResourcesPath,
            final String appWebUiResourcesPath,
            final String loginTargetPlatformSpecificPath,
            final String mobileAndDesktopAppSpecificPath,
            final Function<String, String[]> commandMaker,
            final String[] additionalPaths) {
        if (LOGGER == null) {
            throw new IllegalArgumentException("Logger is a required argumet.");
        }

        LOGGER.info("Vulcanizing...");
        final ISourceController sourceController = injector.getInstance(ISourceController.class);

        final IWebUiConfig webUiConfig = injector.getInstance(IWebUiConfig.class);

        // create the directory in which all needed resources will reside
        final File dir = new File("vulcan");
        dir.mkdir();

        copyStaticResources(platformVendorResourcesPath, platformWebUiResourcesPath, appVendorResourcesPath, appWebUiResourcesPath, LOGGER);
        LOGGER.info("\t------------------------------");

        LOGGER.info("\tVulcanizing login resources...");
        vulcanizeStartupResourcesFor("login", DeviceProfile.MOBILE, sourceController, loginTargetPlatformSpecificPath, commandMaker.apply("login"), additionalPaths, LOGGER, dir);
        LOGGER.info("\tVulcanized login resources.");

        LOGGER.info("\t------------------------------");

        downloadCommonGeneratedResources(webUiConfig, sourceController, LOGGER);
        LOGGER.info("\t------------------------------");

        LOGGER.info("\tVulcanizing mobile resources...");
        downloadSpecificGeneratedResourcesFor(DeviceProfile.MOBILE, sourceController, LOGGER);
        vulcanizeStartupResourcesFor("mobile", DeviceProfile.MOBILE, sourceController, mobileAndDesktopAppSpecificPath, commandMaker.apply("mobile"), additionalPaths, LOGGER, dir);
        LOGGER.info("\tVulcanized mobile resources.");
        LOGGER.info("\t------------------------------");

        LOGGER.info("\tVulcanizing desktop resources...");
        downloadSpecificGeneratedResourcesFor(DeviceProfile.DESKTOP, sourceController, LOGGER);
        vulcanizeStartupResourcesFor("desktop", DeviceProfile.DESKTOP, sourceController, mobileAndDesktopAppSpecificPath, commandMaker.apply("desktop"), additionalPaths, LOGGER, dir);
        LOGGER.info("\tVulcanized desktop resources.");
        LOGGER.info("\t------------------------------");

        clearObsoleteResources(dir);

        LOGGER.info("Vulcanized.");
    }

    private static void clearObsoleteResources(final File dir) {
        LOGGER.info("\tClear obsolete files...");
        try {
            FileUtils.deleteDirectory(dir);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        LOGGER.info("\tCleared obsolete files.");
    }

    private static void downloadCommonGeneratedResources(final IWebUiConfig webUiConfig, final ISourceController sourceController, final Logger logger) {
        logger.info("\tDownloading common generated resources...");
        downloadSource("app", "tg-reflector.html", sourceController, null, logger);
        for (final Class<? extends AbstractEntity<?>> masterType : webUiConfig.getMasters().keySet()) {
            downloadSource("master_ui", masterType.getName(), sourceController, null, logger);
        }
        for (final Class<? extends MiWithConfigurationSupport<?>> centreMiType : webUiConfig.getCentres().keySet()) {
            downloadSource("centre_ui", centreMiType.getName(), sourceController, null, logger);
            // downloadSource("centre_ui/egi", centreMiType.getName(), sourceController, null, logger);
        }
        for (final String viewName : webUiConfig.getCustomViews().keySet()) {
            downloadSource("custom_view", viewName, sourceController, null, logger);
        }
        logger.info("\tDownloaded common generated resources.");
    }

    private static void downloadSpecificGeneratedResourcesFor(final DeviceProfile deviceProfile, final ISourceController sourceController, final Logger logger) {
        logger.info("\t\tDownloading " + deviceProfile + " generated resources...");
        downloadSource("app", "tg-app-config.html", sourceController, deviceProfile, logger);
        downloadSource("app", "tg-app.html", sourceController, deviceProfile, logger);
        downloadSource("app", "tg-element-loader.html", sourceController, deviceProfile, logger);
        if (DeviceProfile.DESKTOP.equals(deviceProfile)) {
            logger.info("\t\t\tDownloading " + deviceProfile + " generated resource 'desktop-application-startup-resources.html'...");
            downloadSource("app", "desktop-application-startup-resources.html", sourceController, deviceProfile, logger);
        }
        logger.info("\t\tDownloaded " + deviceProfile + " generated resources.");
    }

    private static void vulcanizeStartupResourcesFor(
            final String prefix,
            final DeviceProfile deviceProfile,
            final ISourceController sourceController,
            final String targetAppSpecificPath,
            final String[] commands,
            final String[] additionalPaths,
            final Logger logger,
            final File dir) {

        if (additionalPaths == null) {
            throw new IllegalArgumentException("Argument additionalPaths cannot be null, but can be empty if no additiona paths are required for the PATH env. variable.");
        }

        logger.info("\t\tVulcanizing [" + prefix + "-startup-resources-origin.html]...");
        try {
            final ProcessBuilder pb = new ProcessBuilder(commands);

            // need to enrich the PATH with the paths that point to vulcanize and node
            if (additionalPaths.length > 0) {
                final String addPaths = Arrays.stream(additionalPaths).collect(Collectors.joining(File.pathSeparator));
                final String path = System.getenv().get("PATH");
                pb.environment().put("PATH", String.format("%s%s%s", path, File.pathSeparator, addPaths));
            }

            // redirect error stream to the output
            pb.redirectErrorStream(true);

            // start the process
            final Process process = pb.start();

            // let's build a process output reader that would collect it into a local variable for printing
            // should would include errors and any other output produced by the process
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
                final String output = reader.lines().collect(Collectors.joining("\n"));
                System.out.printf("OUTPUT: \n%s\n", output);
            }

            // wait for the process to complete before doing anything else...
            process.waitFor();

        } catch (final IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);

            // need to clear obsolete resources in case of vulcanization failure
            clearObsoleteResources(dir);

            throw new IllegalStateException(e);
        }
        logger.info("\t\tVulcanized [" + prefix + "-startup-resources-origin.html].");

        logger.info("\t\tInlining styles / scripts in [" + prefix + "-startup-resources-origin-vulcanized.html]...");
        try {
            final FileInputStream fileInputStream = new FileInputStream(prefix + "-startup-resources-origin-vulcanized.html");
            final String vulcanized = IOUtils.toString(fileInputStream, Charsets.UTF_8.name());
            fileInputStream.close();

            final PrintStream ps = new PrintStream(prefix + "-startup-resources-origin-vulcanized.html");
            ps.print(inlineScripts(inlineStyles(vulcanized, sourceController, deviceProfile, logger), sourceController, deviceProfile, logger));
            ps.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\t\tInlined styles / scripts in [" + prefix + "-startup-resources-origin-vulcanized.html].");

        logger.info("\t\tMove vulcanized file to its destination...");
        try {
            FileUtils.copyFile(new File(prefix + "-startup-resources-origin-vulcanized.html"), new File(targetAppSpecificPath + prefix + "-startup-resources-vulcanized.html"));
            new File(prefix + "-startup-resources-origin-vulcanized.html").delete();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\t\tMoved vulcanized file to its destination.");
    }

    /**
     * Copies static resources from the places that should be relative to the application module, in which concrete Vulcanize utility reside.
     *
     * @param platformVendorResourcesPath
     * @param platformWebUiResourcesPath
     * @param appVendorResourcesPath
     * @param appWebUiResourcesPath
     */
    private static void copyStaticResources(final String platformVendorResourcesPath, final String platformWebUiResourcesPath, final String appVendorResourcesPath, final String appWebUiResourcesPath, final Logger logger) {
        logger.info("\tCopying static resources...");
        new File("vulcan/resources").mkdir();
        try {
            // Application resources take precedence over the platform resources. Also our resources take precedence over vendor resources.
            FileUtils.copyDirectory(new File(platformVendorResourcesPath), new File("vulcan/resources"));
            FileUtils.copyDirectory(new File(platformWebUiResourcesPath), new File("vulcan/resources"));
            if (appVendorResourcesPath != null) { // TODO remove if statement
                FileUtils.copyDirectory(new File(appVendorResourcesPath), new File("vulcan/resources"));
            }
            if (appWebUiResourcesPath != null) { // TODO remove if statement
                FileUtils.copyDirectory(new File(appWebUiResourcesPath), new File("vulcan/resources"));
            }
            FileUtils.copyFile(new File("vulcan/resources/desktop-startup-resources-origin.html"), new File("vulcan/desktop-startup-resources-origin.html"));
            FileUtils.copyFile(new File("vulcan/resources/mobile-startup-resources-origin.html"), new File("vulcan/mobile-startup-resources-origin.html"));
            FileUtils.copyFile(new File("vulcan/resources/login-startup-resources-origin.html"), new File("vulcan/login-startup-resources-origin.html"));
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tCopied static resources.");
    }

    private static void downloadSource(final String dir, final String name, final ISourceController sourceController, final DeviceProfile deviceProfile, final Logger logger) {
        PrintStream ps;
        try {
            final File directory = new File("vulcan/" + dir);
            if (!directory.exists()) {
                directory.mkdir();
            }

            final String pathAndName = "/" + dir + "/" + name;
            ps = new PrintStream("vulcan" + pathAndName);
            ps.println(sourceController.loadSource(pathAndName, deviceProfile));
            ps.close();
        } catch (final FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Inlines stylesheets inside the source (vulcanized).
     *
     * Format of styles to be inlined: <link rel="import" type="css" href="/resources/polymer/paper-item/paper-item-shared.css">
     *
     * @param source
     * @param sourceController
     *
     * @return
     */
    private static String inlineStyles(final String source, final ISourceController sourceController, final DeviceProfile deviceProfile, final Logger logger) {
        // TODO FRAGILE APPROACH! please, provide better implementation (whitespaces, exchanged rel, type and href, double or single quotes etc.?):
        final String searchString = "<link rel=\"import\" type=\"css\" href=\"";
        final int indexOfCssImport = source.indexOf(searchString);
        if (indexOfCssImport > -1) {
            final String endSearchString = "\">";
            final int endIndex = source.indexOf(endSearchString, indexOfCssImport + searchString.length()) + endSearchString.length();
            final String importStatement = source.substring(indexOfCssImport, endIndex);
            final String uri = importStatement.substring(searchString.length(), importStatement.length() - endSearchString.length());
            logger.info("\t\t\tInlining style [" + uri + "]...");
            return inlineStyles(source.replace(importStatement, "<style>" + sourceController.loadSource(uri, deviceProfile) + "\n</style>"), sourceController, deviceProfile, logger);
        } else {
            return source;
        }
    }

    /**
     * Manually inlines scripts inside the source (vulcanized).
     *
     * Format of scripts to be inlined: <script src="/resources/lodash/3.5.0/lodash.min.js"></script>
     *
     * @param source
     * @param sourceController
     *
     * @return
     */
    private static String inlineScripts(final String source, final ISourceController sourceController, final DeviceProfile deviceProfile, final Logger logger) {
        // TODO FRAGILE APPROACH! please, provide better implementation (whitespaces, exchanged charset and src, double or single quotes etc.?)
        final String searchString = "<script src=\"/";
        final int indexOfScriptTag = source.indexOf(searchString);
        if (indexOfScriptTag > -1) {
            final String endSearchString = "\"></script>";
            final int endIndex = source.indexOf(endSearchString, indexOfScriptTag + searchString.length() - 1) + endSearchString.length();
            final String scriptTag = source.substring(indexOfScriptTag, endIndex);
            final String uri = scriptTag.substring(searchString.length() - 1, scriptTag.length() - endSearchString.length());
            logger.info("\t\t\tInlining script [" + uri + "]...");
            return inlineScripts(source.replace(scriptTag, "<script>" + sourceController.loadSource(uri, deviceProfile).replace("//# sourceMappingURL", "//") + "\n</script>"), sourceController, deviceProfile, logger);
        } else {
            return source;
        }
    }
}
