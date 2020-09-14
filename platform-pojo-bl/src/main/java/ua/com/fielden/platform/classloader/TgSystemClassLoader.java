package ua.com.fielden.platform.classloader;

import static ua.com.fielden.platform.utils.Pair.pair;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Optional;
import java.util.jar.JarFile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.Pair;

/**
 * A replacement for the system class loader, which has the ability to register derived loaders, and uses them in an attempt to find requested classes.
 *
 * @author TG Team
 *
 */
public class TgSystemClassLoader extends URLClassLoader {

    private final Cache<Class<?>, byte[]> cache = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).concurrencyLevel(50).build();
    
    public long cleanUp() {
        cache.cleanUp();
        return cache.size();
    }

    public TgSystemClassLoader(final ClassLoader parent) {
        super(new URL[0], parent);
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

    /**
     * Needed to support debugging where agents are added dynamically.
     * @see <a href="https://docs.oracle.com/javase/9/docs/api/java/lang/instrument/Instrumentation.html#appendToSystemClassLoaderSearch-java.util.jar.JarFile-">Instrumentation</a>
     * @param jarfile
     * @throws Exception
     */
    void appendToSystemClassLoaderSearch(final JarFile jarfile) throws Exception {
        appendToClassPathForInstrumentation(jarfile.getName());
    }

    /**
     * Needed to support debugging where agents are added dynamically.
     * @see <a href="https://docs.oracle.com/javase/9/docs/api/java/lang/instrument/Instrumentation.html#appendToSystemClassLoaderSearch-java.util.jar.JarFile-">Instrumentation</a>
     * @param jarfile
     * @throws Exception
     */
    void appendToClassPathForInstrumentation(final String path) throws Exception {
        addURL(new File(path).toURI().toURL());
    }

    /**
     * Overridden to increase visibility for {@link ClassesRetriever} to access.
     */
	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

    public Optional<Pair<Class<?>, byte[]>> classByName(final String name) {
        return cache.asMap().entrySet().stream().filter(entry -> entry.getKey().getName().equals(name)).findFirst().map(entry -> pair(entry.getKey(), entry.getValue()));
    }
    
    public TgSystemClassLoader cacheClassDefinition(final Class<?> typeAsClass, final byte[] typeAsBytes) {
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
            return DynamicEntityClassLoader.getInstance(this).loadClass(name);
        }
    }
}
