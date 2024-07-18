package ua.com.fielden.platform.entity.proxy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Reflector;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.AbstractEntity.PROXIED_PROPERTY_NAMES_METHOD_NAME;

/**
 * 
 * A class that represents runtime type information for a proxied entity type. 
 * Factory method {@link #proxy(Class, String...)} should be used to create proxied entity type.
 * 
 * @author TG Team
 */
public class EntityProxyContainer {

    private static final MethodDelegation proxyChecker = MethodDelegation.to(ProxyPropertyInterceptor.class);
    
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<Object, Class<? extends AbstractEntity<?>>>>
            TYPES = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(10000).concurrencyLevel(50).build();
    
    public static long cleanUp() {
        TYPES.cleanUp();
        return TYPES.size();
    }

    private EntityProxyContainer() {}

    /**
     * Factory method for creating entity type proxies.
     * 
     * @param entityType  entity that is the owner of the properties to be proxied
     * @param propNames  the names of properties to be proxied
     * @param interfaces  interfaces for the proxied type to implement
     */
    public static <T extends AbstractEntity<?>>
    Class<? extends T> proxy(final Class<T> entityType, final Collection<String> propNames,
                             final List<? extends Class> interfaces) {
        // if there is nothing to proxy then we can simply return the same type
        if (propNames.isEmpty()) {
            return entityType;
        }

        final Set<String> uniquePropNames = ImmutableSet.copyOf(propNames);

        final var typeKey = makeTypeKey(uniquePropNames, interfaces);
        // let's try to find the generated type in the cache
        final var typeCache = getOrCreateTypeCache(entityType);
        final Class<? extends AbstractEntity<?>> type = typeCache.getIfPresent(typeKey);
        if (type != null) {
            return (Class<? extends T>) type;
        }

        Builder<T> buddy = new ByteBuddy()
                .subclass(entityType);

        // try to avoid implementing IProxyEntity more than once
        if (interfaces.stream().noneMatch(IProxyEntity.class::isAssignableFrom)) {
            buddy = buddy.implement(IProxyEntity.class);
        }

        buddy = buddy.implement(interfaces);

        for (final String propName: uniquePropNames) {
            final Method accessor = Reflector.obtainPropertyAccessor(entityType, propName);
            final Method setter = Reflector.obtainPropertySetter(entityType, propName);
    
            buddy = buddy
                .method(ElementMatchers.named(accessor.getName()))
                    .intercept(proxyChecker)
                .method(ElementMatchers.named(setter.getName()))
                    .intercept(proxyChecker);
        }
        
        final Class<? extends T> proxyType = buddy
            .method(ElementMatchers.named(PROXIED_PROPERTY_NAMES_METHOD_NAME))
            .intercept(FixedValue.value(ImmutableSet.copyOf(uniquePropNames)))
            .make()
            // use class loader of the entity being proxied instead of a general system class loader,
            // since it might have been loaded by a different class loader (e.g. a child class loader)
            .load(entityType.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        typeCache.put(typeKey, proxyType);
        return proxyType;
    }

    /**
     * Factory method for creating entity type proxies.
     *
     * @param entityType -- entity that is the owner of the properties to be proxied
     * @param propNames -- the names of properties to be proxied
     */
    public static <T extends AbstractEntity<?>>
    Class<? extends T> proxy(final Class<T> entityType, final Collection<String> propNames) {
        return proxy(entityType, propNames, List.of());
    }

    /**
     * Factory method for creating entity type proxies.
     *
     * @param entityType -- entity that is the owner of the properties to be proxied
     * @param propNames -- the names of properties to be proxied
     */
    public static <T extends AbstractEntity<?>> Class<? extends T> proxy(final Class<T> entityType, final String... propNames) {
        return proxy(entityType, Arrays.asList(propNames), List.of());
    }

    protected static <T extends AbstractEntity<?>>
    Cache<Object, Class<? extends AbstractEntity<?>>> getOrCreateTypeCache(final Class<T> entityType) {
        try {
            return TYPES.get(entityType, () -> CacheBuilder.newBuilder().weakValues().build());
        } catch (final ExecutionException ex) {
            throw new RuntimeException("Failed to create type cache.", ex);
        }
    }

    /**
     * Creates a unique key for a type.
     *
     * @param properties  properties to be proxied in the type
     * @param interfaces  interfaces for the type to implement
     */
    protected static Object makeTypeKey(Collection<String> properties, Collection<? extends Class> interfaces) {
        final String propertiesKey = properties.stream().distinct().sorted(String::compareTo).collect(joining(","));
        final String interfacesKey = interfaces.stream().distinct().map(Class::getCanonicalName).sorted(String::compareTo).collect(joining(","));
        return Stream.of(propertiesKey, interfacesKey).filter(s -> !s.isEmpty()).collect(joining(":"));
    }

}
