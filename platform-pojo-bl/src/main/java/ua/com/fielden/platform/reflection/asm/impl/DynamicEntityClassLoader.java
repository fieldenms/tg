package ua.com.fielden.platform.reflection.asm.impl;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.exceptions.DynamicEntityClassLoaderException;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.asm.impl.TypeMaker.GET_ORIG_TYPE_METHOD_NAME;
import static ua.com.fielden.platform.types.tuples.T2.t2;

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
    private static final Logger LOGGER = getLogger(DynamicEntityClassLoader.class);
    private static final String INF_DEFINED_NEW_TYPE = "Gen: %s; defined new [%s] type.";
    /**
     * A cache of instances of this type of the form: {@code parentClassLoader -> thisInstance}.
     */
    private static final DynamicEntityClassLoader instance = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

    /**
     * A cache of generated types.
     * The key values are the full names of the generated types, which is used as a convenience to get the generated type by name.
     * The value is a tuple, containing a generated type and the original type, used to produce the generated one.
     */
    private static final ConcurrentMap<String, T2<WeakReference<Class<?>>, Class<?>>> CACHE = new ConcurrentHashMap<>(/*initialCapacity*/ 1000, /*loadFactor*/ 0.75f, /*concurrencyLevel*/50);

    /**
     * Optionally returns a cached generated class by {@code className}.
     *
     * @param className
     * @return
     */
    public static Optional<Class<?>> getCachedClass(final String className) {
        return ofNullable(CACHE.get(className)).map(t2 -> t2._1.get());
    }

    private DynamicEntityClassLoader(final ClassLoader parent) {
        super(parent, /*sealed*/ false);
    }

    /**
     * Runs a cleanup routine on the cache of generated classes, registered by the dynamic class loader.
     * 
     * @return a total number of generated classes current cached.
     */
    public static long cleanUp() {
        int count = 0;
        for (final var entry : CACHE.entrySet()) {
            try {
                final var t3 = entry.getValue();
                if (t3 == null || t3._1.get() == null) {
                    CACHE.remove(entry.getKey());
                    count++;
                }
            } catch (final Exception ex) {
                LOGGER.error("Error occurred during cache cleanup.", ex);
            }
        }
        LOGGER.info("Cache size [%s]. Evicted [%s] entries from cache.".formatted(CACHE.size(), count));
        return CACHE.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Class<?>> doDefineClasses(final Map<String, byte[]> typeDefinitions) throws ClassNotFoundException {
        return typeDefinitions.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> doDefineClass(entry.getKey(), entry.getValue())));
    }

    /**
     * Returns already loaded class with {@code name}, if present.
     * Otherwise, performs {@link #defineClass(String, byte[])} and caches entry in form {@code [genTypeName : (genType, origType)]}.
     */
    private Class<?> doDefineClass(final String name, final byte[] bytes) {
        return CACHE.computeIfAbsent(name, key -> {
            // define the class, load it and cache for later reuse
            final Class<?> klass = defineClass(name, bytes, 0, bytes.length);
            LOGGER.info(INF_DEFINED_NEW_TYPE.formatted(CACHE.size(), klass.getSimpleName()));
            return t2(new WeakReference<>(klass), determineOriginalType(klass));
        })._1.get();
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
        final var t3 = CACHE.get(type.getName());
        if (t3 != null) {
            return (Class<T>) t3._2;
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

}