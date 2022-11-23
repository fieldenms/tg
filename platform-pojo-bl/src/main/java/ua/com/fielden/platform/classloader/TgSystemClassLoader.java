package ua.com.fielden.platform.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import ua.com.fielden.platform.reflection.ClassesRetriever;

/**
 * A replacement for the system class loader, which has the ability to register derived loaders, and uses them in an attempt to find requested classes.
 *
 * @author TG Team
 *
 */
public class TgSystemClassLoader extends URLClassLoader {

    public TgSystemClassLoader(final ClassLoader parent) {
        super(new URL[0], parent);
    }

    /**
     * Overridden to increase visibility for {@link ClassesRetriever} to access.
     */
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

}