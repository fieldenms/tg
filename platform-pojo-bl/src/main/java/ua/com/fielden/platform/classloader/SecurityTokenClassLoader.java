package ua.com.fielden.platform.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import ua.com.fielden.platform.reflection.ClassesRetriever;

/**
 * This URL class loader exposes method {@link #addURL(URL)} to support dynamic loading of classes from files.
 * The main need for this class loader is to be able to find and load dynamically classes that represent security tokens.  
 *
 * @author TG Team
 *
 */
public class SecurityTokenClassLoader extends URLClassLoader {

    public SecurityTokenClassLoader(final ClassLoader parent) {
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