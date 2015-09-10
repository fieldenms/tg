package ua.com.fielden.platform.web.test.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.google.common.base.Charsets;
import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/**
 * Web UI vulcanization launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class Vulcanize {
    private static final Logger logger = Logger.getLogger(Vulcanize.class);

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

        // needs to be overridden to start vulcanization in development mode (no need to calculate preloaded resources)
        props.setProperty("workflow", "development");

        DOMConfigurator.configure(props.getProperty("log4j"));

        logger.info("Starting app...");
        final TgTestApplicationConfiguration component = new TgTestApplicationConfiguration(props);
        logger.info("Started app.");

        vulcanize(component.injector());
    }

    /**
     * Vulcanizes 'startup-resources-origin.html' file into 'startup-resources-vulcanized.html'.
     *
     * @param injector
     */
    private static void vulcanize(final Injector injector) {
        logger.info("Vulcanizing...");
        final ISourceController sourceController = injector.getInstance(ISourceController.class);

        final IWebUiConfig webUiConfig = injector.getInstance(IWebUiConfig.class);

        // create the directory in which all needed resources will reside
        final File dir = new File("vulcan");
        dir.mkdir();

        logger.info("\tCopying static resources...");
        new File("vulcan/resources").mkdir();
        try {
            FileUtils.copyDirectory(new File("../platform-web-ui/src/main/resources"), new File("vulcan/resources"));
            FileUtils.copyDirectory(new File("../platform-web-ui/src/main/web/ua/com/fielden/platform/web"), new File("vulcan/resources"));
            FileUtils.copyFile(new File("vulcan/resources/desktop-startup-resources-origin.html"), new File("vulcan/desktop-startup-resources-origin.html"));
            FileUtils.copyFile(new File("vulcan/resources/mobile-startup-resources-origin.html"), new File("vulcan/mobile-startup-resources-origin.html"));
            FileUtils.copyFile(new File("vulcan/resources/login-startup-resources-origin.html"), new File("vulcan/login-startup-resources-origin.html"));
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tCopied static resources.");

        logger.info("\tDownloading generated resources...");
        downloadSource("app", "tg-app-config.html", sourceController, null);
        downloadSource("app", "tg-reflector.html", sourceController, null);
        for (final Class<? extends AbstractEntity<?>> masterType: webUiConfig.getMasters().keySet()) {
            downloadSource("master_ui", masterType.getName(), sourceController, null);
        }
        for (final Class<? extends MiWithConfigurationSupport<?>> centreMiType: webUiConfig.getCentres().keySet()) {
            downloadSource("centre_ui", centreMiType.getName(), sourceController, null);
        }






        logger.info("\tVulcanizing [login-startup-resources-origin.html]...");
        try {
            final ProcessBuilder pb = new ProcessBuilder("C:/Users/Yuriy/AppData/Roaming/npm/vulcanize.cmd", "-p", "\"vulcan/\"", "/login-startup-resources-origin.html", ">", "login-startup-resources-origin-vulcanized.html");
            pb.redirectErrorStream(true);
            final Process process = pb.start();
            process.waitFor();
        } catch (final IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tVulcanized [login-startup-resources-origin.html].");

        logger.info("\tInlining styles / scripts in [login-startup-resources-origin-vulcanized.html]...");
        try {
            final FileInputStream fileInputStream = new FileInputStream("login-startup-resources-origin-vulcanized.html");
            final String vulcanized = IOUtils.toString(fileInputStream, Charsets.UTF_8.name());
            fileInputStream.close();

            final PrintStream ps = new PrintStream("login-startup-resources-origin-vulcanized.html");
            ps.print(inlineScripts(inlineStyles(vulcanized, sourceController, DeviceProfile.MOBILE), sourceController, DeviceProfile.MOBILE));
            ps.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tInlined styles / scripts in [login-startup-resources-origin-vulcanized.html].");

        logger.info("\tMove vulcanized file to its destination and clear obsolete files...");
        try {
            FileUtils.copyFile(new File("login-startup-resources-origin-vulcanized.html"), new File("../platform-web-ui/src/main/web/ua/com/fielden/platform/web/login-startup-resources-vulcanized.html"));
            // FileUtils.deleteDirectory(dir);
            new File("login-startup-resources-origin-vulcanized.html").delete();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tMoved vulcanized file to its destination and cleared obsolete files.");






        logger.info("\tDownloading generated resources (MOBILE DeviceProfile)...");
        downloadSource("app", "tg-app.html", sourceController, DeviceProfile.MOBILE);
        downloadSource("app", "tg-element-loader.html", sourceController, DeviceProfile.MOBILE);
        logger.info("\tDownloaded generated resources.");

        logger.info("\tVulcanizing [mobile-startup-resources-origin.html]...");
        try {
            final ProcessBuilder pb = new ProcessBuilder("C:/Users/Yuriy/AppData/Roaming/npm/vulcanize.cmd", "-p", "\"vulcan/\"", "/mobile-startup-resources-origin.html", ">", "mobile-startup-resources-origin-vulcanized.html");
            pb.redirectErrorStream(true);
            final Process process = pb.start();
            process.waitFor();
        } catch (final IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tVulcanized [mobile-startup-resources-origin.html].");

        logger.info("\tInlining styles / scripts in [mobile-startup-resources-origin-vulcanized.html]...");
        try {
            final FileInputStream fileInputStream = new FileInputStream("mobile-startup-resources-origin-vulcanized.html");
            final String vulcanized = IOUtils.toString(fileInputStream, Charsets.UTF_8.name());
            fileInputStream.close();

            final PrintStream ps = new PrintStream("mobile-startup-resources-origin-vulcanized.html");
            ps.print(inlineScripts(inlineStyles(vulcanized, sourceController, DeviceProfile.MOBILE), sourceController, DeviceProfile.MOBILE));
            ps.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tInlined styles / scripts in [mobile-startup-resources-origin-vulcanized.html].");

        logger.info("\tMove vulcanized file to its destination and clear obsolete files...");
        try {
            FileUtils.copyFile(new File("mobile-startup-resources-origin-vulcanized.html"), new File("../platform-web-ui/src/main/web/ua/com/fielden/platform/web/mobile-startup-resources-vulcanized.html"));
            // FileUtils.deleteDirectory(dir);
            new File("mobile-startup-resources-origin-vulcanized.html").delete();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tMoved vulcanized file to its destination and cleared obsolete files.");










        logger.info("\tDownloading generated resources (DESKTOP DeviceProfile)...");
        downloadSource("app", "tg-app.html", sourceController, DeviceProfile.DESKTOP);
        downloadSource("app", "tg-element-loader.html", sourceController, DeviceProfile.DESKTOP);
        logger.info("\tDownloaded generated resources.");

        logger.info("\tVulcanizing [desktop-startup-resources-origin.html]...");
        try {
            final ProcessBuilder pb = new ProcessBuilder("C:/Users/Yuriy/AppData/Roaming/npm/vulcanize.cmd", "-p", "\"vulcan/\"", "/desktop-startup-resources-origin.html", ">", "desktop-startup-resources-origin-vulcanized.html");
            pb.redirectErrorStream(true);
            final Process process = pb.start();
            process.waitFor();
        } catch (final IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tVulcanized [desktop-startup-resources-origin.html].");

        logger.info("\tInlining styles / scripts in [desktop-startup-resources-origin-vulcanized.html]...");
        try {
            final FileInputStream fileInputStream = new FileInputStream("desktop-startup-resources-origin-vulcanized.html");
            final String vulcanized = IOUtils.toString(fileInputStream, Charsets.UTF_8.name());
            fileInputStream.close();

            final PrintStream ps = new PrintStream("desktop-startup-resources-origin-vulcanized.html");
            ps.print(inlineScripts(inlineStyles(vulcanized, sourceController, DeviceProfile.DESKTOP), sourceController, DeviceProfile.DESKTOP));
            ps.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tInlined styles / scripts in [desktop-startup-resources-origin-vulcanized.html].");

        logger.info("\tMove vulcanized file to its destination and clear obsolete files...");
        try {
            FileUtils.copyFile(new File("desktop-startup-resources-origin-vulcanized.html"), new File("../platform-web-ui/src/main/web/ua/com/fielden/platform/web/desktop-startup-resources-vulcanized.html"));
            FileUtils.deleteDirectory(dir);
            new File("desktop-startup-resources-origin-vulcanized.html").delete();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        logger.info("\tMoved vulcanized file to its destination and cleared obsolete files.");



        logger.info("Vulcanized.");
    }

    private static void downloadSource(final String dir, final String name, final ISourceController sourceController, final DeviceProfile deviceProfile) {
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
    private static String inlineStyles(final String source, final ISourceController sourceController, final DeviceProfile deviceProfile) {
        // TODO FRAGILE APPROACH! please, provide better implementation (whitespaces, exchanged rel, type and href, double or single quotes etc.?):
        final String searchString = "<link rel=\"import\" type=\"css\" href=\"";
        final int indexOfCssImport = source.indexOf(searchString);
        if (indexOfCssImport > -1) {
            final String endSearchString = "\">";
            final int endIndex = source.indexOf(endSearchString, indexOfCssImport + searchString.length()) + endSearchString.length();
            final String importStatement = source.substring(indexOfCssImport, endIndex);
            final String uri = importStatement.substring(searchString.length(), importStatement.length() - endSearchString.length());
            logger.info("\t\tInlining style [" + uri + "]...");
            return inlineStyles(source.replace(importStatement, "<style>" + sourceController.loadSource(uri, deviceProfile) + "\n</style>"), sourceController, deviceProfile);
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
    private static String inlineScripts(final String source, final ISourceController sourceController, final DeviceProfile deviceProfile) {
        // TODO FRAGILE APPROACH! please, provide better implementation (whitespaces, exchanged charset and src, double or single quotes etc.?)
        final String searchString = "<script src=\"";
        final int indexOfScriptTag = source.indexOf(searchString);
        if (indexOfScriptTag > -1) {
            final String endSearchString = "\"></script>";
            final int endIndex = source.indexOf(endSearchString, indexOfScriptTag + searchString.length()) + endSearchString.length();
            final String scriptTag = source.substring(indexOfScriptTag, endIndex);
            final String uri = scriptTag.substring(searchString.length(), scriptTag.length() - endSearchString.length());
            logger.info("\t\tInlining script [" + uri + "]...");
            return inlineScripts(source.replace(scriptTag, "<script>" + sourceController.loadSource(uri, deviceProfile).replace("//# sourceMappingURL", "//") + "\n</script>"), sourceController, deviceProfile);
        } else {
            return source;
        }
    }
}