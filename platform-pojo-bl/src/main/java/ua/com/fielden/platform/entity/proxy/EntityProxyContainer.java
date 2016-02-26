package ua.com.fielden.platform.entity.proxy;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    
    public final Class<? extends T> entityType;
    public final Map<String, ProxyPropertyInterceptor> propertyInterceptors;
    
    private EntityProxyContainer(final Class<? extends T> type, final List<ProxyPropertyInterceptor> propertyInterceptors) {
        this.entityType = type;
        
        final Map<String, ProxyPropertyInterceptor> map = new HashMap<>();
        for (final ProxyPropertyInterceptor interceptor: propertyInterceptors) {
            map.put(interceptor.proxiedPropName, interceptor);
        }
        
        this.propertyInterceptors = Collections.unmodifiableMap(map);
    }
    
    
    /**
     * Factory method for creating entity type proxies.
     * 
     * @param entityType -- entity that is the owner of the properties to be proxied
     * @param propNames -- the names of properties to be proxied
     * @return
     */
    public static <T extends AbstractEntity<?>> EntityProxyContainer<T> proxy(final Class<T> entityType, final String... propNames) {
        // if there is nothing to proxy then we can simply return the same type
        if (propNames.length == 0) {
            return new EntityProxyContainer<T>(entityType, new ArrayList<>());
        }
        
        // a list to collect all created interceptors
        final List<ProxyPropertyInterceptor> propertyInterceptors = new ArrayList<>();
        
        // let's use a set to avoid potential property duplicates
        final Set<String> properties = new HashSet<>();
        properties.addAll(Arrays.asList(propNames));
        
        Builder<T> buddy = new ByteBuddy().subclass(entityType);
        for (final String propName: properties) {
            final ProxyPropertyInterceptor interceptor = new ProxyPropertyInterceptor(entityType, propName);
            final MethodDelegation delegation = MethodDelegation.to(interceptor).filter(ElementMatchers.namedIgnoreCase("accessInterceptor"));
    
            propertyInterceptors.add(interceptor);
    
            final Method accessor = Reflector.obtainPropertyAccessor(entityType, propName);
            final Method setter = Reflector.obtainPropertySetter(entityType, propName);
    
            buddy = buddy
                .method(ElementMatchers.named(accessor.getName()))
                    .intercept(delegation)
                    .annotateMethod(accessor.getAnnotations())
                .method(ElementMatchers.named(setter.getName()))
                    .intercept(delegation)
                    .annotateMethod(setter.getAnnotations());

        }
        
        final Class<? extends T> ownerType = buddy
            .method(ElementMatchers.named("proxiedPropertyNames"))
            .intercept(FixedValue.value(Collections.unmodifiableSet(properties)))
            .make()
            .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        
        return new EntityProxyContainer<T>(ownerType, propertyInterceptors);
    }
}
