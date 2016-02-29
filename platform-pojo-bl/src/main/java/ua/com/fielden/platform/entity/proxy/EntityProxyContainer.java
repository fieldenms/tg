package ua.com.fielden.platform.entity.proxy;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * 
 * A class that represents runtime type information for a proxied entity type. 
 * Factory method {@link #proxy(Class, String...)} should be used to create proxied entity type.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class EntityProxyContainer<T extends AbstractEntity<?>> {

    private static final MethodDelegation proxyChecker = MethodDelegation.to(ProxyPropertyInterceptor.class);
    
    private EntityProxyContainer() {
    }
    
    
    /**
     * Factory method for creating entity type proxies.
     * 
     * @param entityType -- entity that is the owner of the properties to be proxied
     * @param propNames -- the names of properties to be proxied
     * @return
     */
    public static <T extends AbstractEntity<?>> Class<? extends T> proxy(final Class<T> entityType, final String... propNames) {
        // if there is nothing to proxy then we can simply return the same type
        if (propNames.length == 0) {
            return entityType;
        }
        
        // let's use a set to avoid potential property duplicates
        final Set<String> properties = new HashSet<>();
        properties.addAll(Arrays.asList(propNames));
        
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
            .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        
        return ownerType;
    }
}
