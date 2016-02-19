package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.DefinersExecutor;


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
            final R instatiatedFully = instantiator.instantiateFully(entityContainer, justAddedEntity);
            DefinersExecutor.execute(instatiatedFully);
            return instatiatedFully;
        }
     
        return (R) existingEntity;
    }
}