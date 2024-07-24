package ua.com.fielden.platform.entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.com.google.common.collect.ImmutableMap.toImmutableMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.reflection.Reflector.obtainPropertySetter;

/**
 * Provides dynamic name-based access (read & write) to property values of entity instances.
 */
public final class DynamicPropertyAccess {

    public static final DynamicPropertyAccess INSTANCE = new DynamicPropertyAccess();

    /**
     * Returns the value of the named property in {@code entity}. Fails if the named property cannot be located.
     */
    public Object getProperty(final AbstractEntity<?> entity, final String prop) {
        final VarHandle vh = getData((Class<? extends AbstractEntity<?>>) entity.getClass()).propertyHandles.get(prop);
        if (vh == null) {
            throw new IllegalArgumentException("Failed to locate property [%s] in entity [%s]".formatted(
                    prop, entity.getType().getTypeName()));
        }

        return vh.get(entity);
    }

    /**
     * Assigns the value to the named property in {@code entity}. Fails if the named property cannot be located.
     */
    public void setProperty(final AbstractEntity<?> entity, final String prop, final Object value) {
        final MethodHandle setter = getData((Class<? extends AbstractEntity<?>>) entity.getClass()).setters.get(prop);
        if (setter == null) {
            throw new IllegalArgumentException("Failed to locate setter for property [%s] in entity [%s]".formatted(
                    prop, entity.getType().getTypeName()));
        }

        try {
            setter.invoke(entity, value);
        } catch (final Throwable e) {
            throw e instanceof RuntimeException re ? re : new RuntimeException(e);
        }
    }

    /**
     * Scans an entity type and builds an internal data structure, if it's not already built, to enable efficient dynamic
     * property access.
     * <p>
     * In general, such internal data structures are built on demand, but this method can be used to build them ahead of time.
     */
    public void scan(final Class<? extends AbstractEntity<?>> entityType) {
        getData(entityType);
    }

    /**
     * @see #scan(Class)
     */
    public void scan(final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes) {
        entityTypes.forEach(this::scan);
    }

    // --- Implementation

    private final Map<Class<?>, Data> entityDataMap = new ConcurrentHashMap<>();

    /**
     * @param propertyHandles  for reading property values, keyed on property names
     * @param setters  for writing property values, keyed on property names
     */
    private record Data (Map<String, VarHandle> propertyHandles,
                         Map<String, MethodHandle> setters)
    {}

    private Data getData(final Class<? extends AbstractEntity<?>> entityType) {
        final var data = entityDataMap.get(entityType);
        if (data != null) {
            return data;
        }
        final var newData = buildData(entityType);
        entityDataMap.put(entityType, newData);
        return newData;
    }

    private Data buildData(final Class<? extends AbstractEntity<?>> entityType) {
        final var lookupProvider = new CachingPrivateLookupProvider();

        return Stream.concat(Stream.of(getFieldByName(entityType, ID),
                                       getFieldByName(entityType, VERSION)),
                             streamRealProperties(entityType))
                .collect(Collectors.teeing(
                        toImmutableMap(Field::getName, lookupProvider::unreflect),
                        toImmutableMap(Field::getName, prop -> lookupProvider.unreflect(obtainPropertySetter(entityType, prop.getName()))),
                        Data::new));
    }

    private static VarHandle unreflect(final MethodHandles.Lookup lookup, final Field field) {
        try {
            return lookup.unreflectVarHandle(field);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle unreflect(final MethodHandles.Lookup lookup, final Method method) {
        try {
            return lookup.unreflect(method);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private DynamicPropertyAccess() {}

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

        public VarHandle unreflect(final Field field) {
            return DynamicPropertyAccess.unreflect(apply(field.getDeclaringClass()), field);
        }

        public MethodHandle unreflect(final Method method) {
            return DynamicPropertyAccess.unreflect(apply(method.getDeclaringClass()), method);
        }
    }

}
