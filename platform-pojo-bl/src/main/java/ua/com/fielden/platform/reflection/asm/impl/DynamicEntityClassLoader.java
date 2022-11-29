package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.reflection.asm.impl.TypeMaker.GET_ORIG_TYPE_METHOD_NAME;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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
    /**
     * A cache of instances of this type of the form: {@code parentClassLoader -> thisInstance}.
     */
    private static final DynamicEntityClassLoader instance = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

    private static final Cache<Class<?>, T2<byte[], Class<?>>> cache = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).concurrencyLevel(50).build();

    private DynamicEntityClassLoader(final ClassLoader parent) {
        super(parent, /*sealed*/ false);
    }

    /**
     * Runs a cleanup routine on the cache of generated classes, registered by the dynamic class loader.
     * 
     * @return a total number of generated classes current cached.
     */
    public static long cleanUp() {
        cache.cleanUp();
        return cache.size();
    }

    /**
     * Tries to retrieve a type with <code>typeName</code> from cache.
     *
     * @param typeName - fully-qualified name in binary form (i.e., the result of {@link Class#getName()}.
     * @return
     */
    private static Optional<Pair<Class<?>, byte[]>> lookupCache(final String typeName) {
        return cache.asMap().entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(typeName))
                .findFirst()
                    .map(entry -> pair(entry.getKey(), entry.getValue()._1));
    }
    
    /**
     * Creates a new cache entry that maps {@link Class} to its byte representation.
     * If an entry for the given {@link Class} already exists, it will be overwritten.
     *
     * @param typePair
     */
    private static void cacheClass(final Pair<Class<?>, byte[]> typePair) {
        cache.put(typePair.getKey(), t2(typePair.getValue(), determineOriginalType(typePair.getKey())));
    }

    /**
     * Returns an array of bytes for a cached {@link Class} with binary name equal to <code>name</code>.
     *
     * @param name binary name of a class
     * @return byte array representing a cached class or <code>null</code> if the class wasn't found
     */
    public static byte[] getCachedByteArray(final String name) {
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
     * A static wrapper to call {@code loadClass} for the singleton.
     * 
     * @param typeName
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadType(final String typeName) throws ClassNotFoundException {
        return instance.loadClass(typeName);
    }

    /**
     * Initiates modification of the given type. This could be either a dynamic or a static type (created manually by a developer).
     *
     * @param origType
     * @return
     * @throws ClassNotFoundException
     */
    public static <T> TypeMaker<T> startModification(final Class<T> origType) throws ClassNotFoundException {
        return new TypeMaker<T>(instance, origType).startModification();
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
     * Returns an original type for the specified one (the type from which {@code type} was generated).
     * The expectation is that all generated types get cached before they are used.
     * <p> 
     * If {@code type} is not a generated one - simply returns that type back.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> Class<T> getOriginalType(final Class<?> type) {
        final var t2 = cache.getIfPresent(type);
        if (t2 != null) {
            return (Class<T>) t2._2;
        } else {
            return (Class<T>) type;
        }
    }

    /**
     * Determines the original type by using static method with name {@code GET_ORIG_TYPE_METHOD_NAME}, which is expected to be present in all generated types.
     *
     * @param <T>
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractEntity<?>> Class<T> determineOriginalType(final Class<?> type) {
        try {
            final Method getOrigType = type.getMethod(GET_ORIG_TYPE_METHOD_NAME);
            return (Class<T>) getOrigType.invoke(null);
        } catch (final Exception ex) {
            throw new DynamicEntityClassLoaderException("Could not determine the original type for generated type [%s]".formatted(type.getSimpleName()), ex);
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