package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyFactory;
import ua.com.fielden.platform.entity.proxy.ProxyMode;


public class ProxyCache {
    private Map<Class<? extends AbstractEntity<?>>, Map<Long, Object>> map = new HashMap<>();
    
    public Object getProxy(final Class<? extends AbstractEntity<?>> entityType, final Long id) {
        final Map<Long, Object> existingTypeCache = map.get(entityType);
        
        if (existingTypeCache == null) {
            final Map<Long, Object> justAddedTypeCache = new HashMap<Long, Object>();
            final Object justAddedProxy = instantiateProxy(entityType, id); 
            justAddedTypeCache.put(id, justAddedProxy);
            map.put(entityType, justAddedTypeCache);
            return justAddedProxy;
        }
        
        final Object existingProxy = existingTypeCache.get(id);
        if (existingProxy == null) {
            final Object justAddedProxy = instantiateProxy(entityType, id);
            existingTypeCache.put(id, justAddedProxy);
            return justAddedProxy;
        }
     
        return existingProxy;
    }
    
    private <E extends AbstractEntity<?>> Object instantiateProxy(final Class<E> entityType, final Long id  ) {
        final EntityProxyFactory<?> epf = new EntityProxyFactory<>(entityType);
        return epf.create(id, null, null, null, ProxyMode.STRICT);
    }
}
