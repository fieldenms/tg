package ua.com.fielden.platform.web.vulcanizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.TgAppActionsResource;
import ua.com.fielden.platform.web.vulcanizer.exceptions.VulcanisationException;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.io.File.pathSeparator;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.lang.System.getenv;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.cypher.Checksum.sha256;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

/**
 * A set of utilities to facilitate Web UI application vulcanization.
 *
 * @author TG Team
 *
 */
public class VulcanizingUtility {
    private static final Logger LOGGER = getLogger(VulcanizingUtility.class);
    
    public static String[] unixCommands(final String action) {
        return new String[] {"/bin/bash", action + "-script.bat"};
    }

    public static String[] windowsCommands(final String action) {
        // JVM arguments (brackets should be removed): [src/main/resources/application.properties "C:/Program Files/nodejs;C:/Users/Yuriy/AppData/Roaming/npm"]
        return new String[] {"CMD", "/c", action + "-script.bat"};
    }

    protected static T3<Properties, String[], String[]> processVmArguments(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new VulcanisationException(
                    "One or two arguments are expected: \n" +
                    "\t1st is the path to the application properties file;\n" +
                    "\t2nd is the additional paths to be added to the PATH env. variable.\n" +
                    "\t3nd and more should represent environment variables in a form of NAME=VALUE.\n");
        }
        final String propertyFile = args[0];
        final String paths;
        final String[] envVars;
        if (args.length == 1) {
            paths = "";
            envVars = new String[0];
        } else {
            paths = args[1];
            envVars = new String[args.length - 2];
            arraycopy(args, 2, envVars, 0, args.length - 2);
        }

        try {
            final Properties props = retrieveApplicationPropertiesAndConfigureLogging(propertyFile);
            final String[] additionalPaths = paths.split(pathSeparator);
            return t3(props, additionalPaths, envVars);
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
            final String[] additionalPaths,
            final String[] envVarPairs,
            final String... externalResources) {
        if (LOGGER == null) {
            throw new VulcanisationException("Logger is a required argumet.");
        }

        try {
            LOGGER.info("Vulcanizing...");
            final IWebResourceLoader webResourceLoader = injector.getInstance(IWebResourceLoader.class);
            final IWebUiConfig webUiConfig = injector.getInstance(IWebUiConfig.class);

            // create the directory in which all needed resources will reside
            final File dir = new File("vulcan");
            dir.mkdir();

            copyStaticResources(platformVendorResourcesPath, platformWebUiResourcesPath, appVendorResourcesPath, appWebUiResourcesPath);

            final String loginPrefix = "login-";
            LOGGER.info(format("\tVulcanizing [%s] resources...", loginPrefix));
            adjustRootResources(webResourceLoader, loginPrefix);
            vulcanizeStartupResourcesFor(loginPrefix, loginTargetPlatformSpecificPath, commandMaker.apply("build"), commandMaker.apply("minify"), additionalPaths, envVarPairs, dir);
            LOGGER.info(format("\tVulcanized [%s] resources.", loginPrefix));

            downloadGeneratedResources(webUiConfig, webResourceLoader);

            final String prefix = "";
            LOGGER.info(format("\tVulcanizing [%s] resources...", prefix));
            adjustRootResources(webResourceLoader, prefix);
            vulcanizeStartupResourcesFor(prefix, mobileAndDesktopAppSpecificPath, commandMaker.apply("build"), commandMaker.apply("minify"), additionalPaths, envVarPairs, dir);
            LOGGER.info(format("\tVulcanized [%s] resources...", prefix));

            LOGGER.info(format("\tGenerating checksums..."));
            final List<String> allExternalResources = listOf(
                "/app/tg-app-index.html",
                "/resources/startup-resources-vulcanized.js",
                "/resources/polymer/@webcomponents/webcomponentsjs/webcomponents-bundle.js",
                "/resources/polymer/web-animations-js/web-animations-next-lite.min.js",
                "/resources/filesaver/FileSaver.min.js",
                "/resources/manifest.webmanifest",
                "/resources/icons/tg-icon192x192.png",
                "/resources/icons/tg-icon144x144.png",
                // Other page trees (logout.html, login.html, login-initiate-reset.html, login-initiated-reset.html).
                // Please note that login.html cannot go through service worker caching due to the need to redirect to index.html when authenticator appears.
                // And logout.html cannot go through service worker to support request redirection during Single Log-Out lifecycle.
                "/resources/zxcvbn/zxcvbn.js",
                "/resources/login-startup-resources-vulcanized.js",
                "/resources/icons/tg-icon.png",
                "/app/login-initiate-reset.html",
                "/resources/login-initiated-reset.html",
                "/resources/graphiql/graphiql.min.css",
                "/resources/graphiql/react.production.min.js",
                "/resources/graphiql/react-dom.production.min.js",
                "/resources/graphiql/graphiql.min.js",
                "/resources/gis/images/arrow-blue.png",
                "/resources/gis/images/arrow-green.png",
                "/resources/gis/images/circle-orange.png",
                "/resources/gis/images/circle-purple.png",
                "/resources/gis/images/circle-red.png",
                "/resources/gis/images/circle-black.png",
                "/resources/gis/images/triangle.png",
                "/resources/gis/leaflet/images/empty-image.gif",
                "/resources/gis/leaflet/images/layers-2x.png",
                "/resources/gis/leaflet/images/layers.png",
                "/resources/gis/leaflet/images/marker-icon-2x.png",
                "/resources/gis/leaflet/images/marker-icon.png",
                "/resources/gis/leaflet/images/marker-shadow.png",
                "/resources/gis/leaflet/controlloading/images/control-loading.gif",
                "/resources/gis/leaflet/draw/images/spritesheet-2x.png",
                "/resources/gis/leaflet/draw/images/spritesheet-2x.png",
                "/resources/gis/leaflet/draw/images/spritesheet.svg",
                "/resources/gis/leaflet/easybutton/fontawesome/fonts/fontawesome-webfont.eot",
                "/resources/gis/leaflet/easybutton/fontawesome/fonts/fontawesome-webfont.svg",
                "/resources/gis/leaflet/easybutton/fontawesome/fonts/fontawesome-webfont.ttf",
                "/resources/gis/leaflet/easybutton/fontawesome/fonts/fontawesome-webfont.woff",
                "/resources/gis/leaflet/easybutton/fontawesome/fonts/fontawesome-webfont.woff2"
            );
            allExternalResources.addAll(listOf(externalResources));
            final Map<String, String> checksums = generateChecksums(allExternalResources.toArray(new String[0]));
            try {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(new File(mobileAndDesktopAppSpecificPath + prefix + "checksums.json"), checksums);
            } catch (final IOException ex) {
                final String msg = "Could not write checksum.json";
                LOGGER.error(msg, ex);
                throw new VulcanisationException(msg, ex);
            }

            LOGGER.info(format("\tGenerated checksums... [%s]", checksums));
        } catch (final Exception ex) {
            LOGGER.fatal(ex.getMessage(), ex);
            throw ex;
        } finally {
            clearObsoleteResources();
        }
        LOGGER.info("Vulcanized.");
    }

    /**
     * Generates SHA256 checksums for specified <code>paths</code>.
     *
     * @param paths
     * @return
     */
    private static Map<String, String> generateChecksums(final String ... paths) {
        final Map<String, String> checksums = new LinkedHashMap<>();
        for (final String path: asList(paths)) {
            try {
                final FileInputStream fileInputStream = new FileInputStream("vulcan" + path);
                final String sha = sha256(fileInputStream).getKey();
                checksums.put(path, sha);
            } catch (final Exception ex) {
                final String msg = format("Could not generate checksum for resource [%s].", path);
                LOGGER.error(msg, ex);
                throw new VulcanisationException(msg, ex);
            }
        }
        return checksums;
    }

    /**
     * Copies template for 'rollup.config.js' and adjusts it according to <code>profile</code>.
     *
     * @param webResourceLoader
     * @param profile
     */
    private static void adjustRootResources(final IWebResourceLoader webResourceLoader, final String profile) {
        try {
            FileUtils.copyFile(new File("vulcan/resources/rollup.config.js"), new File("rollup.config.js"));
            adjustFileContents("rollup.config.js", profile);
            copyFile(new File("vulcan/resources/minify-script.bat"), new File("minify-script.bat"));
            adjustFileContents("minify-script.bat", profile);
            copyFile(new File("vulcan/resources/" + profile + "startup-resources-origin.js"), new File(profile + "startup-resources-origin.js"));
        } catch (final Exception ex) {
            final String msg = "Error occurred while adjusting root resources.";
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }
    }

    /**
     * Replaces '@profile' with concrete <code>profile</code> inside file.
     *
     * @param fileName
     * @param profile
     */
    private static void adjustFileContents(final String fileName, final String profile) {
        final String contents;
        try (final FileInputStream fileInputStream = new FileInputStream(fileName)) {
            contents = IOUtils.toString(fileInputStream, UTF_8.name());
        } catch (final Exception ex) {
            final String msg = format("Could not read file [%s].", fileName);
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }

        try (final PrintStream ps = new PrintStream(fileName)) {
            ps.print(contents.replace("@profile-", profile));
        } catch (final Exception ex) {
            final String msg = format("Could not write file [%s].", fileName);
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }
    }

    /**
     * Removes all intermediate files after vulcanisation.
     */
    private static void clearObsoleteResources() {
        // As described in Java bug JDK-4715154, Windows does not allow a mapped file to be deleted, and the mapped memory buffer is only released when the GC decides to invoke finalize(), or something like that.
        // Therefore, in order to have a reasonable chance to delete all intermediate files after vulcanization, it is necessary to force System.gc() here.
        // While this is only significant for Windows, there should be no ill effects on other operating systems.
        LOGGER.info("\tForce System.gc()...");
        System.gc();
        
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
        } catch (final Exception ex) {
            final String msg = "Exception occurred while cleaning resources after vulcanisation.";
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }
        LOGGER.info("\tCleared obsolete files.");
    }

    /**
     * Downloads generated resources.
     *
     * @param webUiConfig
     * @param webResourceLoader
     */
    private static void downloadGeneratedResources(final IWebUiConfig webUiConfig, final IWebResourceLoader webResourceLoader) {
        LOGGER.info("\tDownloading generated resources...");
        downloadSource("app", "tg-app-index.html", webResourceLoader); // used for checksum generation
        downloadSource("app", "login-initiate-reset.html", webResourceLoader); // used for checksum generation
        downloadSource("app", "tg-reflector.js", webResourceLoader);
        for (final Class<? extends AbstractEntity<?>> masterType : webUiConfig.getMasters().keySet()) {
            downloadSource("master_ui", masterType.getName() + ".js", webResourceLoader);
        }
        for (final String viewName : webUiConfig.getCustomViews().keySet()) {
            downloadSource("custom_view", viewName, webResourceLoader);
        }
        for (final Class<? extends MiWithConfigurationSupport<?>> centreMiType : webUiConfig.getCentres().keySet()) {
            downloadSource("centre_ui", centreMiType.getName() + ".js", webResourceLoader);
        }
        downloadSource("app", "tg-app-config.js", webResourceLoader);
        downloadSource("app", "tg-app.js", webResourceLoader);
        downloadSource("app", TgAppActionsResource.JS_FILE_NAME, webResourceLoader);
        LOGGER.info("\t\t\tDownloading generated resource 'application-startup-resources.js'...");
        downloadSource("app", "application-startup-resources.js", webResourceLoader);
        LOGGER.info("\tDownloaded generated resources.");
    }

    /**
     * Executes vulcanisation process.
     *
     * @param prefix
     * @param targetAppSpecificPath
     * @param vulcanizeCommands
     * @param additionalPaths
     * @param dir
     */
    private static void vulcanizeStartupResourcesFor(
            final String prefix,
            final String targetAppSpecificPath,
            final String[] vulcanizeCommands,
            final String[] minifyCommands,
            final String[] additionalPaths,
            final String[] envVarPairs,
            final File dir) {
        if (additionalPaths == null) {
            throw new VulcanisationException("Argument additionalPaths cannot be null, but can be empty if no additiona paths are required for the PATH env. variable.");
        }
        LOGGER.info("\t\tVulcanizing [" + prefix + "]...");
        processCommands(vulcanizeCommands, additionalPaths, envVarPairs);
        LOGGER.info("\t\tVulcanized [" + prefix + "].");
        LOGGER.info("\t\tMinifying [" + prefix + "]...");
        processCommands(minifyCommands, additionalPaths, envVarPairs);
        LOGGER.info("\t\tMinified [" + prefix + "].");
        LOGGER.info("\t\tMove vulcanized file to its destination...");
        try {
            copyFile(new File(prefix + "startup-resources-vulcanized-minified.js"), new File(targetAppSpecificPath + prefix + "startup-resources-vulcanized.js"));
            copyFile(new File(prefix + "startup-resources-vulcanized-minified.js"), new File("vulcan/resources/" + prefix + "startup-resources-vulcanized.js")); // used for checksum generation
        } catch (final IOException ex) {
            final String msg = "Error occurred during the vulcanization of startup recources.";
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }
        LOGGER.info("\t\tMoved vulcanized file to its destination.");
    }

    private static void processCommands(final String[] commands, final String[] additionalPaths, final String[] envVarPairs) {
        try {
            final ProcessBuilder pb = new ProcessBuilder(commands);
            // need to enrich the PATH with the paths that point to vulcanize and node
            if (additionalPaths.length > 0) {
                final String addPaths = stream(additionalPaths).collect(joining(pathSeparator));
                final String path = getenv().get("PATH");
                final String newPathVal = format("%s%s%s", path, pathSeparator, addPaths);
                LOGGER.info(format("\t\t\tSetting environment variable PATH=%s", newPathVal));
                pb.environment().put("PATH", newPathVal);
            }
            Stream.of(envVarPairs).forEach(pair -> {
                final String[] p = pair.split("=", 2);
                if (p.length != 2) {
                    throw new VulcanisationException(format("Pair name/value [%s] for an environment variable is not formatted correctly.", p));
                }
                LOGGER.info(format("\t\t\tSetting environment variable %s=%s", p[0], p[1]));
                pb.environment().put(p[0], p[1]);
            });

            // redirect error stream to the output
            pb.redirectErrorStream(true);
            // start the process
            final Process process = pb.start();
            // let's build a process output reader that would collect it into a local variable for printing
            // should would include errors and any other output produced by the process
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
                final String output = reader.lines().collect(joining("\n"));
                LOGGER.info(format("\t\t\tOUTPUT: %n%s%n", output));
            }
            // wait for the process to complete before doing anything else...
            process.waitFor();
        } catch (final IOException | InterruptedException ex) {
            final String msg = "Exception occurred while processing commands.";
            LOGGER.error(msg, ex);
            // need to clear obsolete resources in case of failure
            clearObsoleteResources();
            throw new VulcanisationException(msg, ex);
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
        } catch (final IOException ex) {
            final String msg = "Exception occurred while copying static resoruces.";
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }
        LOGGER.info("\tCopied static resources.");
    }

    /**
     * Downloads source into 'vulcan' directory based on concrete <code>deviceProfile</code>.
     *
     * @param dir
     * @param name
     * @param webResourceLoader
     */
    private static void downloadSource(final String dir, final String name, final IWebResourceLoader webResourceLoader) {
        final File directory = new File("vulcan/" + dir);
        if (!directory.exists()) {
            directory.mkdir();
        }
        final String pathAndName = "/" + dir + "/" + name;

        try(final PrintStream ps = new PrintStream("vulcan" + pathAndName)) {
            ps.println(webResourceLoader.loadSource(pathAndName).orElseThrow(() -> new VulcanisationException("Cound not load " + pathAndName)));
        } catch (final FileNotFoundException ex) {
            final String msg = "Exception occurred while downloading web resources.";
            LOGGER.error(msg, ex);
            throw new VulcanisationException(msg, ex);
        }
    }

}
