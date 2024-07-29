package ua.com.fielden.platform.entity;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.reflection.Finder;
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
import static java.lang.invoke.MethodType.methodType;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;
import static ua.com.fielden.platform.reflection.Reflector.obtainPropertySetter;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;

class PropertyIndexerImpl implements PropertyIndexer {

    private final Field idProperty = getFieldByName(AbstractEntity.class, ID);
    private final Field versionProperty = getFieldByName(AbstractEntity.class, VERSION);

    @Override
    public Index indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        if (EntityUtils.isUnionEntityType(entityType)) {
            return buildUnionEntityIndex((Class<? extends AbstractUnionEntity>) entityType);
        }
        return buildIndex(entityType);
    }

    private Index buildIndex(final Class<? extends AbstractEntity<?>> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();

        // 1. Include id and version explicitly, since they lack @IsProperty.
        // 2. We could use streamRealProperties but that would misalign with the old Reflection-based behaviour, which breaks
        // some things. The current approach is already more limiting than the old one, but reasonably so: it provides
        // access only to properties, while the old approach provided access to all fields.
        // 3. Properties of entityType must come before id and version to prioritise any overriden property definitions.
        return distinct(Stream.concat(streamProperties(entityType), Stream.of(idProperty, versionProperty)),
                        Field::getName)
                .collect(Collectors.teeing(
                        toImmutableMap(Field::getName, lookupProvider::unreflectGetter),
                        toImmutableMap(Field::getName, prop -> lookupProvider.unreflect(obtainPropertySetter(entityType, prop.getName()))),
                        StandardIndex::new));
    }


    /**
     * @param getters  for reading property values, keyed on property names
     * @param setters  for writing property values, keyed on property names
     */
    record StandardIndex(Map<String, MethodHandle> getters,
                         Map<String, MethodHandle> setters)
            implements Index
    {
        @Override
        public MethodHandle getter(final String prop) {
            return getters.get(prop);
        }

        @Nullable
        @Override
        public MethodHandle setter(final String prop) {
            return setters.get(prop);
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
    private Index buildUnionEntityIndex(final Class<? extends AbstractUnionEntity> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();

        final var getters = ImmutableMap.<String, MethodHandle>builder();
        final var setters = ImmutableMap.<String, MethodHandle>builder();;

        for (final Field unionProp : AbstractUnionEntity.unionProperties(entityType)) {
            getters.put(unionProp.getName(), lookupProvider.unreflectGetter(unionProp));
            setters.put(unionProp.getName(), lookupProvider.unreflect(obtainPropertySetter(entityType, unionProp.getName())));
        }

        final var unionMembers = Finder.streamUnionMembers(entityType).toList();
        Stream.concat(AbstractUnionEntity.commonProperties(entityType).stream(),
                      Stream.of(KEY, ID, DESC))
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

        return new StandardIndex(getters.buildOrThrow(), setters.buildOrThrow());
    }

    /**
     * Reads the value of a common property from a union entity instance.
     * <p>
     * This method is used to build union entity indices. Ultimately, it gets transformed into a method handle representing
     * a getter (takes 1 argument: a union entity instance). Other arguments are taken care of in the process of method handle
     * transformation. Another way to view this method is as a closure where all arguments except {@code entity} have been
     * captured.
     *
     * @param prop  simple property name
     * @param getters  {@code {canonicalEntityType: getter}}
     * @param entity  entity to retrieve the property value from
     */
    private static Object getCommonProperty(final String prop,
                                            final Map<Class<? extends AbstractEntity<?>>, MethodHandle> getters,
                                            final AbstractUnionEntity entity)
            throws Throwable
    {
        final var activeEntity = entity.activeEntity();
        if (activeEntity == null) {
            return null;
        }

        // active entity might be enhanced, but getters are keyed on canonical entity types
        final var getter = getters.get(activeEntity.getType());
        if (getter == null) {
            throw new IllegalArgumentException(
                    "Failed to resolve common property [%s] in union entity [%s] with active entity [%s]"
                            .formatted(prop, entity.getType().getSimpleName(), activeEntity.getType().getSimpleName()));
        }

        return getter.invoke(activeEntity);
    }

    /**
     * Sets the value of a common property into a union entity instance.
     * <p>
     * Similarly to {@link #getCommonProperty(String, Map, AbstractUnionEntity)}, this method is used to build union entity
     * indices. It gets transformed into a closure with 2 arguments: {@code entity} and {@code value}.
     *
     * @param prop  simple property name
     * @param setters  {@code {canonicalEntityType: getter}}
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
            throw new IllegalArgumentException(
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
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public MethodHandle unreflectGetter(final Field field) {
            try {
                return apply(field.getDeclaringClass()).unreflectGetter(field);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public MethodHandle unreflect(final Method method) {
            try {
                return apply(method.getDeclaringClass()).unreflect(method);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
