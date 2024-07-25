package ua.com.fielden.platform.entity;

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
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;
import static ua.com.fielden.platform.reflection.Reflector.obtainPropertySetter;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;

final class PropertyIndexerImpl implements PropertyIndexer {

    private final Field idProperty = getFieldByName(AbstractEntity.class, ID);
    private final Field versionProperty = getFieldByName(AbstractEntity.class, VERSION);

    @Override
    public Index indexFor(final Class<? extends AbstractEntity<?>> entityType) {
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
                        toImmutableMap(Field::getName, lookupProvider::unreflect),
                        toImmutableMap(Field::getName, prop -> lookupProvider.unreflect(obtainPropertySetter(entityType, prop.getName()))),
                        Index::new));
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

        public VarHandle unreflect(final Field field) {
            try {
                return apply(field.getDeclaringClass()).unreflectVarHandle(field);
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
