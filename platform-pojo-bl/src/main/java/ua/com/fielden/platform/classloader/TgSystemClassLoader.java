package ua.com.fielden.platform.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.Pair;

/**
 * A replacement for the system class loader, which has the ability to register derived loaders, and uses them in an attempt to find requested classes.
 *
 * @author TG Team
 *
 */
public class TgSystemClassLoader extends URLClassLoader {

    private final Cache<Class<?>, byte[]> cache = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).build(); 

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

    public Optional<Pair<Class<?>, byte[]>> classByName(final String name) {
        return cache.asMap().entrySet().stream().filter(entry -> entry.getKey().getName().equals(name)).findFirst().map(entry -> Pair.pair(entry.getKey(), entry.getValue()));
    }
    
    public TgSystemClassLoader putToCache(final Class<?> typeAsClass, final byte[] typeAsBytes) {
        cache.put(typeAsClass, typeAsBytes);
        return this;
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            if (getClass().getName().equals(name)) {
                return Class.forName(getClass().getName());
            }
            return super.findClass(name);
        } catch (final ClassNotFoundException ex) {
            return classByName(name).map(Pair::getKey).orElseThrow(() -> ex);
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
                return DynamicEntityClassLoader.getInstance(this).loadClass(name);
            } catch (final ClassNotFoundException e) {
                throw ex;
            }
        }
    }
}
