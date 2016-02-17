package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

public final class EntityContainer<R extends AbstractEntity<?>> {
    private final Class<R> resultType;
    private final Map<String, Object> primitives = new HashMap<String, Object>();
    private final Map<String, ValueContainer> composites = new HashMap<String, ValueContainer>();
    private final Map<String, EntityContainer<? extends AbstractEntity<?>>> entities = new HashMap<String, EntityContainer<? extends AbstractEntity<?>>>();
    private final Map<String, CollectionContainer<? extends AbstractEntity<?>>> collections = new HashMap<String, CollectionContainer<? extends AbstractEntity<?>>>();
    private boolean proxy = false;
    private boolean strictProxy = false;
    private boolean instrumentalised = false;

    public EntityContainer(final Class<R> resultType) {
        this.resultType = resultType;
    }

    private int countAllDataItems() {
        return primitives.size() + entities.size() + composites.size() + collections.size();
    }

    public boolean notYetInitialised() {
        return countAllDataItems() == 1 && getId() != null && !isUnionEntityType(resultType);
    }

    public boolean isEmpty() {
        if (isUnionEntityType(resultType)) {
            for (final EntityContainer<? extends AbstractEntity<?>> entityContainer : entities.values()) {
                if (!entityContainer.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return (countAllDataItems() == 1 && primitives.containsKey(AbstractEntity.ID) && getId() == null) || (isUnionEntityType(resultType) && countAllDataItems() == 0);
    }

    public Long getId() {
        final Object idObject = primitives.get(AbstractEntity.ID);
        return idObject != null ? new Long(((Number) idObject).longValue())
                : (isUnionEntityType(resultType) ? (entities.values().iterator().hasNext() ? entities.values().iterator().next().getId() : null) : null);
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy() {
        this.proxy = true;
    }

    public boolean isStrictProxy() {
        return strictProxy;
    }

    public void setStrictProxy() {
        this.strictProxy = true;
    }
    
    public void setInstrumentalised() {
        this.instrumentalised = true;
    }
    
    public boolean isInstrumentalised() {
        return instrumentalised;
    }

    public Class<R> getResultType() {
        return resultType;
    }

    public Map<String, Object> getPrimitives() {
        return primitives;
    }

    public Map<String, ValueContainer> getComposites() {
        return composites;
    }

    public Map<String, EntityContainer<? extends AbstractEntity<?>>> getEntities() {
        return entities;
    }

    public Map<String, CollectionContainer<? extends AbstractEntity<?>>> getCollections() {
        return collections;
   }
}