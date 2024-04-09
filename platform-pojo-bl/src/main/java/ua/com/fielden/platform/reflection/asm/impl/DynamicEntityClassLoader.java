package ua.com.fielden.platform.reflection.asm.impl;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.nextTypeName;
import static ua.com.fielden.platform.reflection.asm.impl.TypeMaker.GET_ORIG_TYPE_METHOD_NAME;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.exceptions.DynamicEntityClassLoaderException;
import ua.com.fielden.platform.types.tuples.T2;

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
    private static final String MSG_DEFINED_NEW_TYPE = "Gen: %s; defined new [%s] type.";
    /**
     * Singleton instance of this {@link DynamicEntityClassLoader}.
     */
    private static final DynamicEntityClassLoader instance = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

    /**
     * A cache of generated types.
     * The key values are the full names of the generated types, which is used as a convenience to get the generated type by name.
     * The value is a tuple, containing a generated type and the original type, used to produce the generated one.
     */
    private static final ConcurrentMap<String, T2<Class<?>, Class<?>>> CACHE = new ConcurrentHashMap<>(/*initialCapacity*/ 1000, /*loadFactor*/ 0.75f, /*concurrencyLevel*/50);

    /**
     * Optionally returns a cached generated class by {@code className}.
     *
     * @param className
     * @return
     */
    public static Optional<Class<?>> getCachedClass(final String className) {
        return ofNullable(CACHE.get(className)).map(t2 -> t2._1);
    }

    private DynamicEntityClassLoader(final ClassLoader parent) {
        super(parent, /*sealed*/ false);
    }

    /**
     * Returns a total number of generated classes currently cached.
     */
    public static long size() {
        return CACHE.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Class<?>> doDefineClasses(final Map<String, byte[]> typeDefinitions) {
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
            LOGGER.debug(MSG_DEFINED_NEW_TYPE.formatted(CACHE.size(), klass.getSimpleName()));
            return t2(klass, determineOriginalType(klass));
        })._1;
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
     */
    public static <T> TypeMaker<T> startModification(final Class<T> origType) {
        return new TypeMaker<T>(instance, origType).startModification();
    }

    /**
     * Returns already modified type with {@code typeName} full class name, if there is one.
     * Otherwise generates that type from {@code origType} using {@code modifyType} function (typeMaker -> typeMaker).
     * <p>
     * Typical usage:<br><br>
     * {@code modifiedClass(newTypeName, Entity.class, typeMaker -> typeMaker.addProperties(...).modifyProperties(...).addClassAnnotations(...))}<br><br>
     * Please note that {@code modifyTypeName(newTypeName)} call is not needed in {@code modifyType} function -- {@code newTypeName} will be assigned automatically.
     * Also {@code startModification(origType)} call is not needed.
     * 
     * @param typeName
     * @param modifyType
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Class<? extends T> modifiedClass(final String typeName, final Class<T> origType, final Function<TypeMaker<T>, TypeMaker<T>> modifyType) {
        return (Class<T>) getCachedClass(typeName).orElseGet(() -> modifyType.apply(new TypeMaker<T>(instance, origType).startModification().modifyTypeName(typeName)).endModification());
    }

    /**
     * Generates modified type from {@code origType} using {@code modifyType} function (typeMaker -> typeMaker).
     * <p>
     * Typical usage:<br><br>
     * {@code modifiedClass(origType, typeMaker -> typeMaker.addProperties(...).modifyProperties(...).addClassAnnotations(...))}<br><br>
     * Please note that {@code modifyTypeName(newTypeName)} call may not be used in {@code modifyType} function -- {@link DynamicTypeNamingService#nextTypeName(String)} will be assigned automatically.
     * 
     * @param typeName
     * @param modifyType
     * @return
     */
    public static <T> Class<? extends T> modifiedClass(final Class<T> origType, final Function<TypeMaker<T>, TypeMaker<T>> modifyType) {
        return modifyType.apply(new TypeMaker<T>(instance, origType).startModification()).endModification();
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
        final var t2 = CACHE.get(type.getName());
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

}