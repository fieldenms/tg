package ua.com.fielden.platform.entity.proxy;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * 
 * A class that represents runtime type information for a proxied entity type. 
 * Factory method {@link #proxy(Class, String...)} should be used to create proxied entity type.
 * 
 * @author TG Team
 */
public class EntityProxyContainer {

    private static final MethodDelegation proxyChecker = MethodDelegation.to(ProxyPropertyInterceptor.class);
    
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Class<? extends AbstractEntity<?>>>>
            TYPES = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(10000).concurrencyLevel(50).build();
    
    public static long cleanUp() {
        TYPES.cleanUp();
        return TYPES.size();
    }

    private EntityProxyContainer() {}

    /**
     * Factory method for creating entity type proxies.
     * 
     * @param entityType -- entity that is the owner of the properties to be proxied
     * @param propNames -- the names of properties to be proxied
     */
    public static <T extends AbstractEntity<?>> Class<? extends T> proxy(final Class<T> entityType, final List<String> propNames) {
        // if there is nothing to proxy then we can simply return the same type
        if (propNames.isEmpty()) {
            return entityType;
        }
        
        // let's use a set to avoid potential property duplicates
        // it should be an ordered set to ensure equality between sets for the same propNames, but in different order
        final Set<String> properties = new TreeSet<>(propNames);
        final String key = properties.stream().collect(joining(","));

        // let's try to find the generated type in the cache
        final Cache<String, Class<? extends AbstractEntity<?>>> typeCache = getOrCreateTypeCache(entityType);
        final Class<? extends AbstractEntity<?>> type = typeCache.getIfPresent(key);
        if (type != null) {
            return (Class<? extends T>) type;
        }
        
        Builder<T> buddy = new ByteBuddy().subclass(entityType);

        for (final String propName: properties) {
    
            final Method accessor = Reflector.obtainPropertyAccessor(entityType, propName);
            final Method setter = Reflector.obtainPropertySetter(entityType, propName);
    
            buddy = buddy
                .method(ElementMatchers.named(accessor.getName())) //
                    .intercept(proxyChecker)
                .method(ElementMatchers.named(setter.getName())) // 
                    .intercept(proxyChecker);

        }
        
        final Class<? extends T> ownerType = buddy
            .method(ElementMatchers.named("proxiedPropertyNames"))
            .intercept(FixedValue.value(Collections.unmodifiableSet(properties)))
            .make()
            // use class loader of the entity being proxied instead of a general system class loader,
            // since it might have been loaded by a different class loader (e.g. a child class loader)
            .load(entityType.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        typeCache.put(key, ownerType);
        return ownerType;
    }

    /**
     * Factory method for creating entity type proxies.
     *
     * @param entityType -- entity that is the owner of the properties to be proxied
     * @param propNames -- the names of properties to be proxied
     */
    public static <T extends AbstractEntity<?>> Class<? extends T> proxy(final Class<T> entityType, final String... propNames) {
        return proxy(entityType, Arrays.asList(propNames));
    }

    protected static <T extends AbstractEntity<?>> Cache<String, Class<? extends AbstractEntity<?>>> getOrCreateTypeCache(final Class<T> entityType) {
        try {
            return TYPES.get(entityType, () -> { 
                final Cache<String, Class<? extends AbstractEntity<?>>> newTypeCache = CacheBuilder.newBuilder().weakValues().build();
                TYPES.put(entityType, newTypeCache);
                return newTypeCache;
            });
        } catch (final ExecutionException ex) {
            throw new EntityException("Could not create a proxy type.", ex);
        }
    }
}
