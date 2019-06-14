package ua.com.fielden.platform.web.utils;

import static java.io.File.pathSeparator;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.log4j.xml.DOMConfigurator.configure;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

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
    
    public static String[] unixCommands(final String action) {
        return new String[] {"/bin/bash", action + "-script.bat"};
    }
    
    public static String[] windowsCommands(final String action) {
        // JVM arguments (brackets should be removed): [src/main/resources/application.properties "C:/Program Files/nodejs;C:/Users/Yuriy/AppData/Roaming/npm"]
        return new String[] {"CMD", "/c", action + "-script.bat"};
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
            final String[] additionalPaths = paths.split(pathSeparator);
            return pair(props, additionalPaths);
        } catch (final IOException ex) {
            LOGGER.fatal(format("Application property file %s could not be located or its values are not recognised.", propertyFile), ex);
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
        configure(props.getProperty("log4j"));
        return props;
    }
    
    /**
     * Vulcanizes '*-startup-resources-origin.js' file into '*-startup-resources-vulcanized.js'.
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
        
        try {
            LOGGER.info("Vulcanizing...");
            final ISourceController sourceController = injector.getInstance(ISourceController.class);
            final IWebUiConfig webUiConfig = injector.getInstance(IWebUiConfig.class);

            // create the directory in which all needed resources will reside
            final File dir = new File("vulcan");
            dir.mkdir();

            copyStaticResources(platformVendorResourcesPath, platformWebUiResourcesPath, appVendorResourcesPath, appWebUiResourcesPath);

            final String loginPrefix = "login-";
            LOGGER.info(format("\tVulcanizing [%s] resources...", loginPrefix));
            adjustRootResources(sourceController, loginPrefix);
            vulcanizeStartupResourcesFor(loginPrefix, sourceController, loginTargetPlatformSpecificPath, commandMaker.apply("build"), commandMaker.apply("minify"), additionalPaths, dir);
            LOGGER.info(format("\tVulcanized [%s] resources.", loginPrefix));

            downloadGeneratedResources(webUiConfig, sourceController);

            final String prefix = "";
            LOGGER.info(format("\tVulcanizing [%s] resources...", prefix));
            adjustRootResources(sourceController, prefix);
            vulcanizeStartupResourcesFor(prefix, sourceController, mobileAndDesktopAppSpecificPath, commandMaker.apply("build"), commandMaker.apply("minify"), additionalPaths, dir);
            LOGGER.info(format("\tVulcanized [%s] resources...", prefix));
        } finally {
            clearObsoleteResources();
        }
        LOGGER.info("Vulcanized.");
    }
    
    /**
     * Copies template for 'rollup.config.js' and adjusts it according to <code>profile</code>.
     * 
     * @param sourceController
     * @param logger
     * @param profile
     */
    private static void adjustRootResources(final ISourceController sourceController, final String profile) {
        try {
            FileUtils.copyFile(new File("vulcan/resources/rollup.config.js"), new File("rollup.config.js"));
            adjustFileContents("rollup.config.js", profile);
            copyFile(new File("vulcan/resources/minify-script.bat"), new File("minify-script.bat"));
            adjustFileContents("minify-script.bat", profile);
            copyFile(new File("vulcan/resources/" + profile + "startup-resources-origin.js"), new File(profile + "startup-resources-origin.js"));
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Replaces '@profile' with concrete <code>profile</code> inside file.
     * 
     * @param fileName
     * @param profile
     */
    private static void adjustFileContents(final String fileName, final String profile) {
        try {
            final FileInputStream fileInputStream = new FileInputStream(fileName);
            final String contents = IOUtils.toString(fileInputStream, UTF_8.name());
            fileInputStream.close();
            final PrintStream ps = new PrintStream(fileName);
            ps.print(contents.replace("@profile-", profile));
            ps.close();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Removes all intermediate files after vulcanisation.
     */
    private static void clearObsoleteResources() {
        LOGGER.info("\tClear obsolete files...");
        try {
            deleteDirectory(new File("vulcan"));
            deleteDirectory(new File("build"));
            new File("build-script.bat").delete();
            new File("minify-script.bat").delete();
            new File("rollup.config.js").delete();
            new File("login-startup-resources-origin.js").delete();
            new File("login-startup-resources-vulcanized.js").delete();
            new File("login-startup-resources-vulcanized-minified.js").delete();
            new File("startup-resources-origin.js").delete();
            new File("startup-resources-vulcanized.js").delete();
            new File("startup-resources-vulcanized-minified.js").delete();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        LOGGER.info("\tCleared obsolete files.");
    }
    
    /**
     * Downloads generated resources.
     * 
     * @param webUiConfig
     * @param sourceController
     */
    private static void downloadGeneratedResources(final IWebUiConfig webUiConfig, final ISourceController sourceController) {
        LOGGER.info("\tDownloading generated resources...");
        downloadSource("app", "tg-reflector.js", sourceController, null);
        for (final Class<? extends AbstractEntity<?>> masterType : webUiConfig.getMasters().keySet()) {
            downloadSource("master_ui", masterType.getName() + ".js", sourceController, null);
        }
        for (final String viewName : webUiConfig.getCustomViews().keySet()) {
            downloadSource("custom_view", viewName, sourceController, null);
        }
        for (final Class<? extends MiWithConfigurationSupport<?>> centreMiType : webUiConfig.getCentres().keySet()) {
            downloadSource("centre_ui", centreMiType.getName() + ".js", sourceController, null);
        }
        downloadSource("app", "tg-app-config.js", sourceController, null);
        downloadSource("app", "tg-app.js", sourceController, null);
        LOGGER.info("\t\t\tDownloading generated resource 'application-startup-resources.js'...");
        downloadSource("app", "application-startup-resources.js", sourceController, null);
        LOGGER.info("\tDownloaded generated resources.");
    }
    
    /**
     * Executes vulcanisation process.
     * 
     * @param prefix
     * @param sourceController
     * @param targetAppSpecificPath
     * @param vulcanizeCommands
     * @param additionalPaths
     * @param dir
     */
    private static void vulcanizeStartupResourcesFor(
            final String prefix,
            final ISourceController sourceController,
            final String targetAppSpecificPath,
            final String[] vulcanizeCommands,
            final String[] minifyCommands,
            final String[] additionalPaths,
            final File dir) {
        if (additionalPaths == null) {
            throw new IllegalArgumentException("Argument additionalPaths cannot be null, but can be empty if no additiona paths are required for the PATH env. variable.");
        }
        LOGGER.info("\t\tVulcanizing [" + prefix + "]...");
        processCommands(vulcanizeCommands, additionalPaths);
        LOGGER.info("\t\tVulcanized [" + prefix + "].");
        LOGGER.info("\t\tMinifying [" + prefix + "]...");
        processCommands(minifyCommands, additionalPaths);
        LOGGER.info("\t\tMinified [" + prefix + "].");
        LOGGER.info("\t\tMove vulcanized file to its destination...");
        try {
            copyFile(new File(prefix + "startup-resources-vulcanized-minified.js"), new File(targetAppSpecificPath + prefix + "startup-resources-vulcanized.js"));
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        LOGGER.info("\t\tMoved vulcanized file to its destination.");
    }

    private static void processCommands(final String[] commands, final String[] additionalPaths) {
        try {
            final ProcessBuilder pb = new ProcessBuilder(commands);
            // need to enrich the PATH with the paths that point to vulcanize and node
            if (additionalPaths.length > 0) {
                final String addPaths = stream(additionalPaths).collect(joining(pathSeparator));
                final String path = getenv().get("PATH");
                pb.environment().put("PATH", format("%s%s%s", path, pathSeparator, addPaths));
            }
            // redirect error stream to the output
            pb.redirectErrorStream(true);
            // start the process
            final Process process = pb.start();
            // let's build a process output reader that would collect it into a local variable for printing
            // should would include errors and any other output produced by the process
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
                final String output = reader.lines().collect(joining("\n"));
                System.out.printf("OUTPUT: \n%s\n", output);
            }
            // wait for the process to complete before doing anything else...
            process.waitFor();
        } catch (final IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            // need to clear obsolete resources in case of failure
            clearObsoleteResources();
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Copies static resources from the places that should be relative to the application module, in which concrete Vulcanize utility reside.
     *
     * @param platformVendorResourcesPath
     * @param platformWebUiResourcesPath
     * @param appVendorResourcesPath
     * @param appWebUiResourcesPath
     */
    private static void copyStaticResources(final String platformVendorResourcesPath, final String platformWebUiResourcesPath, final String appVendorResourcesPath, final String appWebUiResourcesPath) {
        LOGGER.info("\tCopying static resources...");
        try {
            new File("vulcan/resources").mkdir();
            // Application resources take precedence over the platform resources. Also our resources take precedence over vendor resources.
            copyDirectory(new File(platformVendorResourcesPath), new File("vulcan/resources"));
            copyDirectory(new File(platformWebUiResourcesPath), new File("vulcan/resources"));
            if (appVendorResourcesPath != null) {
                copyDirectory(new File(appVendorResourcesPath), new File("vulcan/resources"));
            }
            if (appWebUiResourcesPath != null) {
                copyDirectory(new File(appWebUiResourcesPath), new File("vulcan/resources"));
            }
            copyFile(new File("vulcan/resources/build-script.bat"), new File("build-script.bat"));
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        LOGGER.info("\tCopied static resources.");
    }
    
    /**
     * Downloads source into 'vulcan' directory based on concrete <code>deviceProfile</code>.
     * 
     * @param dir
     * @param name
     * @param sourceController
     * @param deviceProfile
     */
    private static void downloadSource(final String dir, final String name, final ISourceController sourceController, final DeviceProfile deviceProfile) {
        PrintStream ps;
        try {
            final File directory = new File("vulcan/" + dir);
            if (!directory.exists()) {
                directory.mkdir();
            }
            final String pathAndName = "/" + dir + "/" + name;
            ps = new PrintStream("vulcan" + pathAndName);
            ps.println(sourceController.loadSource(pathAndName));
            ps.close();
        } catch (final FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }
    
}