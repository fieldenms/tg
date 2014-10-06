package ua.com.fielden.platform.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * This is a helper class for loading resources, which correctly handles path resolution for resources bundled in a jar.
 *
 * @author TG Team
 *
 */
public class ResourceLoader {

    private static final Logger logger = Logger.getLogger(ResourceLoader.class);

    public static Image getImage(final String pathAndFileName) {
        try {
            return Toolkit.getDefaultToolkit().getImage(getURL(pathAndFileName));
        } catch (final Exception e) {
            logger.error("Error loading " + pathAndFileName + ". Cause: " + e.getMessage(), e);
            return null;
        }
    }

    public static ImageIcon getIcon(final String pathAndFileName) {
        try {
            return new ImageIcon(getImage(pathAndFileName));
        } catch (final Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static String getText(final String pathAndFileName) {
	try {
	    return IOUtils.toString(getURL(pathAndFileName).openStream(), "UTF-8");
	} catch (final Exception e) {
	    logger.error(e.getMessage());
	    return null;
	}
    }

    public static URL getURL(final String pathAndFileName) {
        return Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
    }
}
