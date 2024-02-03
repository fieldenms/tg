package ua.com.fielden.platform.utils;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;


/**
 * This is a helper class for loading resources, which correctly handles path resolution for resources bundled in a jar.
 *
 * @author TG Team
 *
 */
public class ResourceLoader {

    private static final String ERR_COULD_NOT_LOAD = "Could not load resource [%s].";
    private static final Logger LOGGER = getLogger(ResourceLoader.class);

    public static Image getImage(final String pathAndFileName) {
        try {
            return Toolkit.getDefaultToolkit().getImage(getURL(pathAndFileName));
        } catch (final Exception ex) {
            LOGGER.error(format(ERR_COULD_NOT_LOAD, pathAndFileName), ex);
            return null;
        }
    }

    public static ImageIcon getIcon(final String pathAndFileName) {
        try {
            return new ImageIcon(getImage(pathAndFileName));
        } catch (final Exception ex) {
            LOGGER.error(format(ERR_COULD_NOT_LOAD, pathAndFileName), ex);
            return null;
        }
    }

    /**
     * Loads the text file content and returns it as UTF-8 string.
     *
     * @param pathAndFileName
     * @return
     */
    public static String getText(final String pathAndFileName) {
        try {
            return IOUtils.toString(getStream(pathAndFileName), "UTF-8");
        } catch (final Exception ex) {
            LOGGER.error(format(ERR_COULD_NOT_LOAD, pathAndFileName), ex);
            return null;
        }
    }

    /**
     * Determines whether resource exists or not.
     *
     * @param pathAndFileName
     * @return
     */
    public static boolean exist(final String pathAndFileName) {
        return getURL(pathAndFileName) != null;
    }

    /**
     * Returns the InputStream for given resource path. Returns null if the resource doesn't exists.
     *
     * @param pathAndFileName
     * @return
     */
    public static InputStream getStream(final String pathAndFileName) {
        try {
            return getURL(pathAndFileName).openStream();
        } catch (final Exception ex) {
            LOGGER.error(format(ERR_COULD_NOT_LOAD, pathAndFileName), ex);
            return null;
        }
    }

    /**
     * Returns the URL for given resource path. Returns null if the resource doesn't exists.
     *
     * @param pathAndFileName
     * @return
     */
    public static URL getURL(final String pathAndFileName) {
        return Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
    }

    /**
     * Returns Optional<Path> for given resource path.
     * Performs URL -> URI -> Path conversion to convert %20 to spaces. 
     * Returns Optional.empty() if the resource doesn't exists.
     *
     * @param pathAndFileName
     * @return
     */
    public static Optional<Path> getPath(final String pathAndFileName) {
        final URL url = getURL(pathAndFileName);
        if (url == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Paths.get(url.toURI()));
            } catch (final Exception ex) {
                LOGGER.error(format(ERR_COULD_NOT_LOAD, pathAndFileName), ex);
                return Optional.empty();
            }
        }
    }
}
