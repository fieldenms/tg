package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;


public class EntityFromContainerInstantiatorCache {
    private Map<EntityContainer<? extends AbstractEntity<?>>, Object> map = new HashMap<>();
    private final EntityFromContainerInstantiator instantiator;
    
    public EntityFromContainerInstantiatorCache(final EntityFromContainerInstantiator instantiator) {
        this.instantiator = instantiator;
    }
    
    public <R extends AbstractEntity<?>> R getEntity(final EntityContainer<R> entityContainer) {
        final AbstractEntity<?> existingEntity = (AbstractEntity<?>) map.get(entityContainer);
        
        if (existingEntity == null) {
            final R justAddedEntity = instantiator.instantiateInitially(entityContainer); 
            map.put(entityContainer, justAddedEntity);
            return instantiator.instantiateFully(entityContainer, justAddedEntity);
        }
     
        return (R) existingEntity;
    }
}