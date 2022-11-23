package ua.com.fielden.platform.reflection.asm.impl;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.asm.impl.TypeMaker.GET_ORIG_TYPE_METHOD_NAME;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.exceptions.DynamicEntityClassLoaderException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

/**
 * A class loader for dynamically constructed or modified entity types.
 * <p>
 * All created types should be loaded by the same instance as used for type modification.
 * The role of {@link InjectionClassLoader} as a parent type is to allow this class loader to be used by ByteBuddy for injecting new types.
 * <p>
 * <b>2022-08-23</b> {@code TgSystemClassLoader} is no longer used as a parent class loader. Caching is implemented directly by this class.
 * <b>2022-11-23</b> {@code DynamicEntityClassLoader} is thread safe.
 *
 * @author TG Team
 */
public class DynamicEntityClassLoader extends InjectionClassLoader {

    private static final String ERR_COULD_NOT_CREAT_CLASS_LOADER = "Could not create a new %s".formatted(DynamicEntityClassLoader.class.getSimpleName());    
    private static final Logger LOGGER = getLogger(DynamicEntityClassLoader.class);
    /**
     * A cache of instances of this type of the form: {@code parentClassLoader -> thisInstance}.
     */
    private static final Cache<ClassLoader, DynamicEntityClassLoader> instances = CacheBuilder.newBuilder().weakKeys().initialCapacity(100).concurrencyLevel(50).build();

    private final Cache<Class<?>, byte[]> cache = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).concurrencyLevel(50).build();

    /**
     * Returns an instance of this class loader type with the specified parent class loader. 
     * The returned instance might be retrieved from cache, which uses {@code parent} as a lookup key.
     *
     * @param parent
     * @return
     */
    public static DynamicEntityClassLoader getInstance(final ClassLoader parent) {
        try {
            return instances.get(parent, () -> new DynamicEntityClassLoader(parent));
        } catch (final ExecutionException ex) {
            LOGGER.error(ERR_COULD_NOT_CREAT_CLASS_LOADER, ex);
            throw new DynamicEntityClassLoaderException(ERR_COULD_NOT_CREAT_CLASS_LOADER, ex);
        }
    }
    
    /**
     * Forcefully creates and returns a new instance of this class loader type with the specified parent class loader regardless of any cached instances.
     * Primarily used in testing environments.
     *
     * @param parent
     * @return
     */
    public static DynamicEntityClassLoader forceInstance(final ClassLoader parent) {
        return new DynamicEntityClassLoader(parent);
    }
    
    private DynamicEntityClassLoader(final ClassLoader parent) {
        super(parent, /*sealed*/ false);
    }

    /**
     * Clears class caches for every registered dynamic class loader.
     *
     * @return a tuple of sizes – the first one for the cache of dynamic class loaders, the second one is a total number of generated classes across all dynamic class loaders.
     */
    public static T2<Long, Long> clearCaches() {
        final AtomicLong count = new AtomicLong(0L);
        instances.asMap().values().forEach(decl -> {decl.clearCache(); count.addAndGet(decl.cache.size());});
        instances.cleanUp();
        instances.invalidateAll();
        return t2(instances.size(), count.get());
    }

    /**
     * Clears class cache.
     */
    public void clearCache() {
        cache.cleanUp();
        cache.invalidateAll();
    }

    /**
     * Tries to retrieve a type with <code>typeName</code> from cache.
     *
     * @param typeName - fully-qualified name in binary form (i.e., the result of {@link Class#getName()}.
     * @return
     */
    private Optional<Pair<Class<?>, byte[]>> lookupCache(final String typeName) {
        return cache.asMap().entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(typeName))
                .findFirst()
                    .map(entry -> pair(entry.getKey(), entry.getValue()));
    }
    
    /**
     * Creates a new cache entry that maps {@link Class} to its byte representation.
     * If an entry for the given {@link Class} already exists, it will be overwritten.
     *
     * @param typePair
     * @return
     */
    public DynamicEntityClassLoader cacheClass(final Pair<Class<?>, byte[]> typePair) {
        cache.put(typePair.getKey(), typePair.getValue());
        return this;
    }

    /**
     * Returns an array of bytes for a cached {@link Class} with binary name equal to <code>name</code>.
     *
     * @param name binary name of a class
     * @return byte array representing a cached class or <code>null</code> if the class wasn't found
     */
    public byte[] getCachedByteArray(final String name) {
        return lookupCache(name).map(Pair::getValue).orElse(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Class<?>> doDefineClasses(final Map<String, byte[]> typeDefinitions) throws ClassNotFoundException {
        return typeDefinitions.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, 
                entry -> doDefineClass(entry.getKey(), entry.getValue())));
    }
    
    protected Class<?> doDefineClass(final String name, final byte[] bytes) {
        return lookupCache(name).map(Pair::getKey).orElseGet(() -> {
            // the class hasn't been defined, so load it and cache for later reuse
            final Class klass = defineClass(name, bytes, 0, bytes.length);
            cacheClass(pair(klass, bytes));
            return klass;
        });
    }
    
    @Override
    public InputStream getResourceAsStream(final String name) {
        // name is a resource name so convert it to binary form
        return lookupCache(toBinaryName(name))
                .map(pair -> (InputStream) new ByteArrayInputStream(pair.getValue()))
                .orElseGet(() -> super.getResourceAsStream(name));
    }
    
    /**
     * Initiates modification of the given type. This could be either a dynamic or a static type (created manually by a developer).
     *
     * @param origType
     * @return
     * @throws ClassNotFoundException
     */
    public <T> TypeMaker<T> startModification(final Class<T> origType) throws ClassNotFoundException {
        return new TypeMaker<T>(this, origType).startModification();
    }

    /**
     * Determines whether <code>type</code> is a generated type.
     * <p>
     * Most likely, generated types have one or more calculated (or custom) property. 
     * But there are also edge-cases. For example, in tests like SerialisationTestResource when a generated type simply has a modified name.
     * 
     * @param type
     * @return
     */
    public static boolean isGenerated(final Class<?> type) {
        return type.getName().contains(DynamicTypeNamingService.APPENDIX);
    }

    /**
     * Returns an original type for the specified one (the type from which <code>type</code> was generated). 
     * If <code>type</code> is not a generated one - simply returns it.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> Class<T> getOriginalType(final Class<?> type) {
        if (isGenerated(type)) {
            try {
                final Method getOrigType = type.getMethod(GET_ORIG_TYPE_METHOD_NAME);
                return (Class<T>) getOrigType.invoke(null);
            } catch (final RuntimeException ex) {
                throw ex;
            } catch (final Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            return (Class<T>) type;
        }
    }

    /**
     * Converts a resource name of a java class to its binary form.
     *
     * @param resourceName
     * @return binary name of a class or null if <code>resourceName</code> doesn't represent a java class.
     */
    private static String toBinaryName(final String resourceName) {
        return resourceName.endsWith(".class") ? StringUtils.substringBeforeLast(resourceName, ".class").replace('/', '.') : null;
    }

}