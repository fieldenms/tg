package ua.com.fielden.platform.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * A replacement for the system class loader, which has the ability to register derived loaders, and uses them in an attempt to find requested classes.
 *
 * @author TG Team
 *
 */
public class TgSystemClassLoader extends URLClassLoader {

    private final DynamicEntityClassLoader dynamicEntityClassLoader = DynamicEntityClassLoader.getInstance(this);

    public TgSystemClassLoader(final ClassLoader parent) {
        super(((URLClassLoader) parent).getURLs(), parent);
    }

    public TgSystemClassLoader(final URL[] urls) {
        super(urls);
    }

    public TgSystemClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
    }

    public TgSystemClassLoader(final URL[] urls, final ClassLoader parent, final URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            if (getClass().getName().equals(name)) {
                return Class.forName(getClass().getName());
            }
            return super.findClass(name);
        } catch (final ClassNotFoundException ex) {
            try {
                return dynamicEntityClassLoader.findClass(name);
            } catch (final ClassNotFoundException e) {
            }
            throw ex;
        }
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        try {
            if (getClass().getName().equals(name)) {
                return Class.forName(getClass().getName());
            }
            return super.loadClass(name);
        } catch (final ClassNotFoundException ex) {
            try {
                return dynamicEntityClassLoader.loadClass(name);
            } catch (final ClassNotFoundException e) {
            }
            throw ex;
        }
    }
}
