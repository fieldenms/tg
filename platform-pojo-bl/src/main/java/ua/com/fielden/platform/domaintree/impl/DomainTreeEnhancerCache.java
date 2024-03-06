package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.types.tuples.T3.t3;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.types.tuples.T3;

/**
 * Default implementation of {@link IDomainTreeEnhancerCache}.
 * 
 * @author TG Team
 *
 */
public class DomainTreeEnhancerCache implements IDomainTreeEnhancerCache {
    private static final ConcurrentMap<T3<Set<Class<?>>, Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>>, DomainTreeEnhancer> domainTreeEnhancers = new ConcurrentHashMap<>();
    
    public static final DomainTreeEnhancerCache CACHE = new DomainTreeEnhancerCache();
    
    private DomainTreeEnhancerCache() {
    }
    
    @Override
    public DomainTreeEnhancer getDomainTreeEnhancerFor(final Set<Class<?>> rootTypes, final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedProperties, final Map<Class<?>, List<CustomProperty>> customProperties) {
        return domainTreeEnhancers.get(t3(rootTypes, calculatedProperties, customProperties));
    }
    
    @Override
    public DomainTreeEnhancer putDomainTreeEnhancerFor(final Set<Class<?>> rootTypes, final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo, final Map<Class<?>, List<CustomProperty>> customProperties, final DomainTreeEnhancer domainTreeEnhancer) {
        domainTreeEnhancers.put(t3(rootTypes, calculatedPropertiesInfo, customProperties), domainTreeEnhancer);
        return domainTreeEnhancer;
    }
    
}