package ua.com.fielden.platform.entity.indexer;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.exceptions.PropertyIndexerException;
import ua.com.fielden.platform.entity.proxy.StrictProxyException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.invoke.MethodType.methodType;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.indexer.PropertyIndexerImpl.StandardPropertyIndex.makeIndex;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.streamDeclaredProperties;
import static ua.com.fielden.platform.reflection.Reflector.obtainPropertySetter;

/**
 * Property indices are built by reusing method handles as much as possible.
 * In general, there will be a single pair of method handles for each property (for its getter and setter).
 * This holds for inherited properties as well.
 * For example, all indices will share the same method handles for properties of {@link AbstractEntity}.
 * <p>
 * One benefit of this extensive reuse is that derived (generated) entity types, which have no additional/modified properties,
 * get property indices for free by reusing those of their original entity types.
 *
 * <h4> Getters </h4>
 * <p>
 * Getters in a property index are method handles that read directly from a {@linkplain VarHandle property's field}.
 * Although this violates encapsulation by ignoring the declared property getter methods, no disadvantages are observed
 * in practice for the majority of property types due to the simplicity of their getters.
 * The only exception is collectional properties, where getters return an unmodifiable view instead of the immediate property value.
 * <p>
 * This is subject to change, with the goal of actually using property getters.
 * However, currently the platform relies on collectional values being read directly from the property's field, which poses a challenge for the implementation of the new approach.
 *
 * <h4> Overridden setters </h4>
 * <p>
 * No special effort is required to make sure that overridden setters are invoked.
 * This is done automatically by {@linkplain MethodHandle#invoke(Object...) method handles}.
 * Similarly to {@linkplain Method#invoke(Object, Object...) Reflection}, a dynamic lookup will be performed based on the receiver argument.
 * 
 * <h5> Glossary of terms: </h5>
 * <p>
 * <ul>
 *   <li> <i>Canonical entity type</i> - an entity type without bytecode enhancements.
 * </ul>
 */
public class PropertyIndexerImpl implements IPropertyIndexer {

    private static final String ERR_MISSING_SETTER = "Missing setter for property [%s] in entity [%s].";
    private static final String ERR_MISSING_GETTER = "Missing getter for property [%s] in entity [%s].";
    private static final String ERR_RESOLVING_COMMON_PROPS_FOR_UNION_ENTITY = "Failed to resolve common property [%s] in union entity [%s] with active entity [%s].";
    private static final String ERR_RESOLVING_SETTER_FOR_COMMON_PROP_OF_UNION_ENTITY = "Failed to resolve a setter for common property [%s] in union entity [%s] with active entity [%s].";

    private static final Field ID_FIELD = getFieldByName(AbstractEntity.class, ID);
    private static final Field VERSION_FIELD = getFieldByName(AbstractEntity.class, VERSION);

    @Override
    public StandardPropertyIndex indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        if (EntityUtils.isUnionEntityType(entityType)) {
            return buildUnionEntityIndex((Class<? extends AbstractUnionEntity>) entityType);
        }
        return buildIndex(entityType);
    }

    private StandardPropertyIndex buildIndex(final Class<? extends AbstractEntity<?>> entityType) {
        // We could use Finder.streamRealProperties but that would diverge from the previous Reflection-based implementation.
        // Primarily, this is due to the conditional inclusion of properties "key" and "desc".
        // The current approach is already more limiting than the previous one, but reasonably so.
        // It provides access only to properties, while before the access was provided to all fields.
        final var declaredPropsIndex = buildDeclaredPropertiesIndex(entityType);
        return ((Class<?>) entityType) == AbstractEntity.class
                ? declaredPropsIndex
                : overlayIndex(declaredPropsIndex, indexFor((Class<? extends AbstractEntity<?>>) entityType.getSuperclass()));
    }

    private static StandardPropertyIndex buildDeclaredPropertiesIndex(final Class<? extends AbstractEntity<?>> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();
        final var properties = (Class<?>) entityType == AbstractEntity.class
                // Include id and version explicitly, since they lack @IsProperty.
                ? Stream.concat(streamDeclaredProperties(entityType), Stream.of(ID_FIELD, VERSION_FIELD))
                : streamDeclaredProperties(entityType);
        return properties.collect(Collectors.teeing(
                toImmutableMap(Field::getName, lookupProvider::unreflectGetter),
                toImmutableMap(Field::getName, prop -> lookupProvider.unreflectSetter(entityType, prop)),
                StandardPropertyIndex::makeIndex));
    }

    private static StandardPropertyIndex overlayIndex(final StandardPropertyIndex top, final StandardPropertyIndex bottom) {
        if (top.isEmpty()) {
            return bottom;
        }
        if (bottom.isEmpty()) {
            return top;
        }

        return makeIndex(overlayMap(top.getters(), bottom.getters()),
                         overlayMap(top.setters(), bottom.setters()));
    }

    private static <K, V> Map<K, V> overlayMap(final Map<K, V> top, final Map<K, V> bottom) {
        if (bottom.isEmpty()) {
            return top;
        }
        if (top.isEmpty()) {
            return bottom;
        }

        final var newMap = ImmutableMap.<K, V>builder();
        // top map entries must go last
        newMap.putAll(bottom);
        newMap.putAll(top);
        return newMap.buildKeepingLast();
    }

    /**
     * @param getters  for reading property values, keyed on property names
     * @param setters  for writing property values, keyed on property names
     */
    record StandardPropertyIndex(
            Map<String, MethodHandle> getters,
            Map<String, MethodHandle> setters)
            implements PropertyIndex
    {
        private static final StandardPropertyIndex EMPTY_INDEX = new StandardPropertyIndex(ImmutableMap.of(), ImmutableMap.of());

        public static StandardPropertyIndex makeIndex(final Map<String, MethodHandle> getters,
                                              final Map<String, MethodHandle> setters) {
            return (getters.isEmpty() && setters.isEmpty()) ? EMPTY_INDEX : new StandardPropertyIndex(getters, setters);
        }

        @Override
        public MethodHandle getter(final String prop) {
            return getters.get(prop);
        }

        @Nullable
        @Override
        public MethodHandle setter(final String prop) {
            return setters.get(prop);
        }

        public boolean isEmpty() {
            return getters.isEmpty() && setters.isEmpty();
        }
    }

    /**
     * Union entity index consists of:
     * <ol>
     *   <li> Union properties (all entity-typed ones).
     *   <li> Common properties, which are accessed through an {@linkplain AbstractUnionEntity#activeEntity() active entity}
     *   of a union entity.
     *        These include properties {@code id}, {@code key} and {@code desc}.
     * </ol>
     */
    private StandardPropertyIndex buildUnionEntityIndex(final Class<? extends AbstractUnionEntity> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();

        final var getters = ImmutableMap.<String, MethodHandle>builder();
        final var setters = ImmutableMap.<String, MethodHandle>builder();

        for (final Field unionProp : AbstractUnionEntity.unionProperties(entityType)) {
            getters.put(unionProp.getName(), lookupProvider.unreflectGetter(unionProp));
            setters.put(unionProp.getName(), lookupProvider.unreflect(obtainPropertySetter(entityType, unionProp.getName())));
        }

        final var unionMembers = Finder.streamUnionMembers(entityType).toList();
        Stream.concat(AbstractUnionEntity.commonProperties(entityType).stream(), Stream.of(KEY, ID, DESC))
               .distinct()
               .forEach(commonPropName -> {
                   // getters for this common property in all union members
                   final Map<Class<?>, MethodHandle> commonPropGetters = unionMembers.stream()
                           .collect(toMap(Function.identity(),
                                          ty -> lookupProvider.unreflectGetter(getFieldByName(ty, commonPropName))));
                   final Map<Class<?>, MethodHandle> commonPropSetters = unionMembers.stream()
                           .collect(toMap(Function.identity(),
                                          ty -> lookupProvider.unreflect(obtainPropertySetter(ty, commonPropName))));
                   // this is where we create closures
                   getters.put(commonPropName, MethodHandles.insertArguments(mh_getCommonProperty, 0, commonPropName, commonPropGetters));
                   setters.put(commonPropName, MethodHandles.insertArguments(mh_setCommonProperty, 0, commonPropName, commonPropSetters));
               });

        return makeIndex(getters.buildOrThrow(), setters.buildOrThrow());
    }

    /**
     * Reads the value of a common property from a union entity instance.
     * <p>
     * This method is used to build union entity indices.
     * Ultimately, it gets transformed into a method handle representing a getter, expecting one argument of a union-entity type.
     * The rest of the arguments are taken care of in the process of a method handle transformation.
     * <p>
     * Another way to view this method is as a closure where all arguments except {@code entity} have been enclosed.
     *
     * @param prop  simple property name
     * @param getters {@code {canonicalEntityType: getter}}
     * @param entity  entity to retrieve the property value from
     */
    private static Object getCommonProperty(
            final String prop,
            final Map<Class<? extends AbstractEntity<?>>, MethodHandle> getters,
            final AbstractUnionEntity entity)
            throws Throwable
    {
        final var activeEntity = entity.activeEntity();
        if (activeEntity == null) {
            return null;
        }

        if (activeEntity.proxiedPropertyNames().contains(prop)) {
            throw new StrictProxyException(ERR_CANNOT_GET_VALUE_FOR_PROXIED_PROPERTY.formatted(prop, activeEntity.getType().getTypeName()));
        }

        // active entity might be enhanced, but getters are keyed on canonical entity types
        final var getter = getters.get(activeEntity.getType());
        if (getter == null) {
            throw new PropertyIndexerException(ERR_RESOLVING_COMMON_PROPS_FOR_UNION_ENTITY.formatted(prop, entity.getType().getSimpleName(), activeEntity.getType().getSimpleName()));
        }

        return getter.invoke(activeEntity);
    }

    /**
     * Sets the value of a common property into a union entity instance.
     * <p>
     * Similarly to {@link #getCommonProperty(String, Map, AbstractUnionEntity)}, this method is used to build union entity indices.
     * It gets transformed into a closure with 2 arguments – {@code entity} and {@code value}.
     *
     * @param prop  simple property name
     * @param setters {@code {canonicalEntityType: setter}}
     * @param entity  union entity to set the value into (effectively, its active entity)
     * @param value  new property value
     */
    private static Object setCommonProperty(
            final String prop,
            final Map<Class<? extends AbstractEntity<?>>, MethodHandle> setters,
            final AbstractUnionEntity entity, final Object value)
            throws Throwable
    {
        final var activeEntity = entity.activeEntity();
        // active entity might be enhanced, but setters are keyed on canonical entity types
        final var setter = setters.get(activeEntity.getType());
        if (setter == null) {
            throw new PropertyIndexerException(ERR_RESOLVING_SETTER_FOR_COMMON_PROP_OF_UNION_ENTITY.formatted(prop, entity.getType().getSimpleName(), activeEntity.getType().getSimpleName()));
        }

        return setter.invoke(activeEntity, value);
    }

    private static final MethodHandle mh_getCommonProperty;
    private static final MethodHandle mh_setCommonProperty;
    static {
        try {
            mh_getCommonProperty = MethodHandles.lookup()
                    .findStatic(PropertyIndexerImpl.class, "getCommonProperty",
                                methodType(Object.class, String.class, Map.class, AbstractUnionEntity.class));
            mh_setCommonProperty = MethodHandles.lookup()
                    .findStatic(PropertyIndexerImpl.class, "setCommonProperty",
                                methodType(Object.class, String.class, Map.class, AbstractUnionEntity.class, Object.class));
        } catch (final NoSuchMethodException | IllegalAccessException ex) {
            throw new PropertyIndexerException(ex);
        }
    }

    /**
     * Seamlessly traverses type hierarchies without worrying about access.
     * <p>
     * For example, retrieving all properties of an entity type requires traversing its type hierarchy.
     * This involves accessing VarHandle's in multiple classes, which requires a different Lookup for each class.
     */
    private static class CachingPrivateLookupProvider implements Function<Class<?>, MethodHandles.Lookup> {
        private final Map<Class<?>, MethodHandles.Lookup> cache = new HashMap<>();

        @Override
        public MethodHandles.Lookup apply(final Class<?> klass) {
            return cache.computeIfAbsent(klass, k -> {
                try {
                    return MethodHandles.privateLookupIn(k, MethodHandles.lookup());
                } catch (final IllegalAccessException ex) {
                    throw new PropertyIndexerException(ex);
                }
            });
        }

        public MethodHandle unreflectGetter(final Field field) {
            try {
                return apply(field.getDeclaringClass()).unreflectGetter(field);
            } catch (final IllegalAccessException ex) {
                throw new ReflectionException(ERR_MISSING_GETTER.formatted(field.getName(), field.getDeclaringClass().getTypeName()), ex);
            }
        }

        public MethodHandle unreflect(final Method method) {
            try {
                return apply(method.getDeclaringClass()).unreflect(method);
            } catch (final IllegalAccessException ex) {
                throw new ReflectionException(ex);
            }
        }

        public MethodHandle unreflectSetter(final Class<? extends AbstractEntity<?>> entityType, final Field field) {
            // Reflector.obtainPropertySetter does not support property "key" overridden in an abstract type, because
            // PropertyTypeDeterminator.determineClass returns null in such cases (due to missing @KeyType), causing an NPE.
            // Therefore, we need to exercise a finer control over the lookup here.
            try {
                return unreflect(Reflector.getMethodForClass(entityType, Mutator.SETTER.getName(field.getName()), field.getType()));
            } catch (final NoSuchMethodException ex) {
                throw new ReflectionException(ERR_MISSING_SETTER.formatted(field.getName(), entityType.getTypeName()), ex);
            }
        }

    }

}
