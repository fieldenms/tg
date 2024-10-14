package ua.com.fielden.platform.entity;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.String.format;
import static java.lang.invoke.MethodType.methodType;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.PropertyIndexerImpl.StandardIndex.makeIndex;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.streamDeclaredProperties;
import static ua.com.fielden.platform.reflection.Reflector.obtainPropertyAccessor;
import static ua.com.fielden.platform.reflection.Reflector.obtainPropertySetter;

/**
 * Property indices are built by reusing method handles as much as possible.
 * In general, there will be a single pair of method handles for each property (for its accessor and setter).
 * This holds for inherited properties as well.
 * For example, all indices will share the same method handles for properties of {@link AbstractEntity}.
 * <p>
 * One benefit of this extensive reuse is that derived (generated) entity types, which have no additional/modified properties,
 * get property indices for free by reusing those of their original entity types.
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
class PropertyIndexerImpl implements PropertyIndexer {

    private final Field idProperty = getFieldByName(AbstractEntity.class, ID);
    private final Field versionProperty = getFieldByName(AbstractEntity.class, VERSION);

    @Override
    public StandardIndex indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        if (EntityUtils.isUnionEntityType(entityType)) {
            return buildUnionEntityIndex((Class<? extends AbstractUnionEntity>) entityType);
        }
        return buildIndex(entityType);
    }

    private StandardIndex buildIndex(final Class<? extends AbstractEntity<?>> entityType) {
        // We could use Finder.streamRealProperties but that would misalign with the old Reflection-based behaviour.
        // Primarily, this is due to conditional inclusion of properties "key" and "desc".
        // The current approach is already more limiting than the old one, but reasonably so.
        // It provides access only to properties, while the old approach provided access to all fields.
        final var declaredPropsIndex = buildDeclaredPropertiesIndex(entityType);
        return ((Class<?>) entityType) == AbstractEntity.class
                ? declaredPropsIndex
                : overlayIndex(declaredPropsIndex, indexFor((Class<? extends AbstractEntity<?>>) entityType.getSuperclass()));
    }

    private StandardIndex buildDeclaredPropertiesIndex(final Class<? extends AbstractEntity<?>> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();
        final var properties = (Class<?>) entityType == AbstractEntity.class
                // Include id and version explicitly, since they lack @IsProperty.
                ? Stream.concat(streamDeclaredProperties(entityType), Stream.of(idProperty, versionProperty))
                : streamDeclaredProperties(entityType);
        return properties.collect(Collectors.teeing(
                toImmutableMap(Field::getName, prop -> lookupProvider.unreflectPropertyAccessor(entityType, prop)),
                toImmutableMap(Field::getName, prop -> lookupProvider.unreflectPropertySetter(entityType, prop)),
                StandardIndex::makeIndex));
    }

    private static StandardIndex overlayIndex(final StandardIndex top, final StandardIndex bottom) {
        if (top.isEmpty()) {
            return bottom;
        }
        if (bottom.isEmpty()) {
            return top;
        }

        return makeIndex(overlayMap(top.accessors(), bottom.accessors()),
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
     * @param accessors  for reading property values, keyed on property names
     * @param setters  for writing property values, keyed on property names
     */
    record StandardIndex(
            Map<String, MethodHandle> accessors,
            Map<String, MethodHandle> setters)
            implements Index
    {
        private static final StandardIndex EMPTY_INDEX = new StandardIndex(ImmutableMap.of(), ImmutableMap.of());

        public static StandardIndex makeIndex(final Map<String, MethodHandle> accessors,
                                              final Map<String, MethodHandle> setters) {
            return (accessors.isEmpty() && setters.isEmpty()) ? EMPTY_INDEX : new StandardIndex(accessors, setters);
        }

        @Override
        public MethodHandle accessor(final String prop) {
            return accessors.get(prop);
        }

        @Nullable
        @Override
        public MethodHandle setter(final String prop) {
            return setters.get(prop);
        }

        public boolean isEmpty() {
            return accessors.isEmpty() && setters.isEmpty();
        }
    }

    /**
     * Union entity index consists of:
     * <ol>
     *   <li> Union properties (all entity-typed ones)
     *   <li> Common properties, which are accessed through an {@linkplain AbstractUnionEntity#activeEntity() active entity}
     *   of a union entity. These include properties {@code id}, {@code key} and {@code desc}.
     * </ol>
     */
    private StandardIndex buildUnionEntityIndex(final Class<? extends AbstractUnionEntity> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();

        final var accessors = ImmutableMap.<String, MethodHandle>builder();
        final var setters = ImmutableMap.<String, MethodHandle>builder();

        for (final Field unionProp : AbstractUnionEntity.unionProperties(entityType)) {
            accessors.put(unionProp.getName(), lookupProvider.unreflect(obtainPropertyAccessor(entityType, unionProp.getName())));
            setters.put(unionProp.getName(), lookupProvider.unreflect(obtainPropertySetter(entityType, unionProp.getName())));
        }

        final var unionMembers = Finder.streamUnionMembers(entityType).toList();
        Stream.concat(AbstractUnionEntity.commonProperties(entityType).stream(),
                      Stream.of(KEY, ID, DESC))
                .distinct()
                .forEach(commonPropName -> {
                    // accessors for this common property in all union members
                    final Map<Class<?>, MethodHandle> commonPropAccessors = unionMembers.stream()
                            .collect(toMap(Function.identity(),
                                           ty -> lookupProvider.unreflect(obtainPropertyAccessor(ty, commonPropName))));
                    final Map<Class<?>, MethodHandle> commonPropSetters = unionMembers.stream()
                            .collect(toMap(Function.identity(),
                                           ty -> lookupProvider.unreflect(obtainPropertySetter(ty, commonPropName))));
                    // this is where we create closures
                    accessors.put(commonPropName, MethodHandles.insertArguments(mh_getCommonProperty, 0, commonPropName, commonPropAccessors));
                    setters.put(commonPropName, MethodHandles.insertArguments(mh_setCommonProperty, 0, commonPropName, commonPropSetters));
                });

        return makeIndex(accessors.buildOrThrow(), setters.buildOrThrow());
    }

    /**
     * Reads the value of a common property from a union entity instance.
     * <p>
     * This method is used to build union entity indices. Ultimately, it gets transformed into a method handle representing
     * a accessor (takes 1 argument: a union entity instance). Other arguments are taken care of in the process of method handle
     * transformation. Another way to view this method is as a closure where all arguments except {@code entity} have been
     * captured.
     *
     * @param prop  simple property name
     * @param accessors  {@code {canonicalEntityType: accessor}}
     * @param entity  entity to retrieve the property value from
     */
    private static Object getCommonProperty(final String prop,
                                            final Map<Class<? extends AbstractEntity<?>>, MethodHandle> accessors,
                                            final AbstractUnionEntity entity)
            throws Throwable
    {
        final var activeEntity = entity.activeEntity();
        if (activeEntity == null) {
            return null;
        }

        // active entity might be enhanced, but accessors are keyed on canonical entity types
        final var accessor = accessors.get(activeEntity.getType());
        if (accessor == null) {
            throw new EntityException(
                    "Failed to resolve common property [%s] in union entity [%s] with active entity [%s]"
                            .formatted(prop, entity.getType().getSimpleName(), activeEntity.getType().getSimpleName()));
        }

        return accessor.invoke(activeEntity);
    }

    /**
     * Sets the value of a common property into a union entity instance.
     * <p>
     * Similarly to {@link #getCommonProperty(String, Map, AbstractUnionEntity)}, this method is used to build union entity
     * indices. It gets transformed into a closure with 2 arguments: {@code entity} and {@code value}.
     *
     * @param prop  simple property name
     * @param setters  {@code {canonicalEntityType: accessor}}
     * @param entity  union entity to set the value into (effectively, its active entity)
     * @param value  new property value
     */
    private static Object setCommonProperty(final String prop,
                                            final Map<Class<? extends AbstractEntity<?>>, MethodHandle> setters,
                                            final AbstractUnionEntity entity, final Object value)
            throws Throwable
    {
        final var activeEntity = entity.activeEntity();
        // active entity might be enhanced, but setters are keyed on canonical entity types
        final var setter = setters.get(activeEntity.getType());
        if (setter == null) {
            throw new EntityException(
                    "Failed to resolve a setter for common property [%s] in union entity [%s] with active entity [%s]"
                            .formatted(prop, entity.getType().getSimpleName(), activeEntity.getType().getSimpleName()));
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
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Allows us to seamlessly traverse type hierarchies without worrying about access.
    // For example, retrieving all properties of an entity type requires traversing its type hierarchy, which involves
    // accessing VarHandle's in multiple classes, which requires a different Lookup for each class.
    private static class CachingPrivateLookupProvider implements Function<Class<?>, MethodHandles.Lookup> {
        private final Map<Class<?>, MethodHandles.Lookup> cache = new HashMap<>();

        @Override
        public MethodHandles.Lookup apply(final Class<?> klass) {
            return cache.computeIfAbsent(klass, k -> {
                try {
                    return MethodHandles.privateLookupIn(k, MethodHandles.lookup());
                } catch (final IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public MethodHandle unreflect(final Method method) {
            try {
                return apply(method.getDeclaringClass()).unreflect(method);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public MethodHandle unreflectPropertyAccessor(final Class<? extends AbstractEntity<?>> entityType, final Field field) {
            return unreflect(obtainPropertyAccessor(entityType, field.getName()));
        }

        public MethodHandle unreflectPropertySetter(final Class<? extends AbstractEntity<?>> entityType, final Field field) {
            // Reflector.obtainPropertySetter does not support property "key" overridden in an abstract type, because
            // PropertyTypeDeterminator.determineClass returns null in such cases (due to missing @KeyType), causing an NPE.
            // Thus, we exercise finer controler over the lookup.
            try {
                return unreflect(Reflector.getMethodForClass(entityType, Mutator.SETTER.getName(field.getName()), field.getType()));
            } catch (final NoSuchMethodException e) {
                throw new ReflectionException(format("Missing setter for property [%s] in entity [%s]",
                                                     field.getName(), entityType.getTypeName()),
                                              e);
            }
        }

    }

}
