package ua.com.fielden.platform.classloader;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * A replacement for the system class loader, which has the ability to register derived loaders, and uses them in an attempt to find requested classes.
 *
 * @author TG Team
 *
 */
public class TgSystemClassLoader extends URLClassLoader {

    private List<WeakReference<DynamicEntityClassLoader>> derivedClassLoaders =  Collections.synchronizedList(new ArrayList<WeakReference<DynamicEntityClassLoader>>());

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

    /**
     * Registers and instance of {@link DynamicEntityClassLoader} with this loader, and purges released weak references to previously registered class loaders.
     *
     * @param classLoader
     * @return
     */
    public TgSystemClassLoader register(final DynamicEntityClassLoader classLoader) {
	if (classLoader.getParent() != this) {
	    throw new IllegalArgumentException("Only class loaders with this parent loader are permitted.");
	}
	derivedClassLoaders.add(new WeakReference<DynamicEntityClassLoader>(classLoader));

	purge();

	return this;
    }

    /**
     * Purge nulled weak references.
     */
    private void purge() {
	for (final Iterator<WeakReference<DynamicEntityClassLoader>> iter = derivedClassLoaders.iterator(); iter.hasNext();) {
	    final WeakReference<DynamicEntityClassLoader> ref = iter.next();
	    if (ref.get() == null) {
		iter.remove();
	    }
	}
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
	try {
	    if (getClass().getName().equals(name)) {
		return Class.forName(getClass().getName());
	    }
	    return super.findClass(name);
	} catch (final ClassNotFoundException ex) {
	    for (final WeakReference<DynamicEntityClassLoader> classLoaderRef : derivedClassLoaders) {
		final DynamicEntityClassLoader cl = classLoaderRef.get();
		if (cl != null) {
		    try {
			return cl.findClass(name);
		    } catch (final ClassNotFoundException e) {
		    }
		}
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
	    for (final WeakReference<DynamicEntityClassLoader> classLoaderRef : derivedClassLoaders) {
		final DynamicEntityClassLoader cl = classLoaderRef.get();
		if (cl != null) {
		    try {
			return cl.loadClass(name);
		    } catch (final ClassNotFoundException e) {
		    }
		}
	    }
	    throw ex;
	}
    }
}
