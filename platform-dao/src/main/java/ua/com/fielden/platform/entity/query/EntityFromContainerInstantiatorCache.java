package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;


public class EntityFromContainerInstantiatorCache {
    private Map<Class<? extends AbstractEntity<?>>, Map<Long, Object>> map = new HashMap<>();
    private final EntityFromContainerInstantiator instantiator;
    
    public EntityFromContainerInstantiatorCache(final EntityFromContainerInstantiator instantiator) {
        this.instantiator = instantiator;
    }
    
    public <R extends AbstractEntity<?>> R getEntity(final EntityContainer<R> entityContainer) {
        final Map<Long, Object> existingTypeCache = map.get(entityContainer.getResultType());
        
        if (existingTypeCache == null) {
            final Map<Long, Object> justAddedTypeCache = new HashMap<Long, Object>();
            final R justAddedEntity = instantiateEntity(entityContainer); 
            justAddedTypeCache.put(entityContainer.getId(), justAddedEntity);
            map.put(entityContainer.getResultType(), justAddedTypeCache);
            return instantiator.instantiateFully(entityContainer, justAddedEntity);
        }
        
        final Object existingEntity = existingTypeCache.get(entityContainer.getId());
        if (existingEntity == null) {
            final R justAddedEntity = instantiateEntity(entityContainer);
            existingTypeCache.put(entityContainer.getId(), justAddedEntity);
            return instantiator.instantiateFully(entityContainer, justAddedEntity);
        }
     
        return (R) existingEntity;
    }
    
    private <R extends AbstractEntity<?>> R instantiateEntity(final EntityContainer<R> entityContainer) {
        return instantiator.instantiateInitially(entityContainer);
    }
}