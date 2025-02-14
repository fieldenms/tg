package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ua.com.fielden.platform.utils.EntityUtils.*;

public final class EntityContainer<R extends AbstractEntity<?>> {
    private final Class<R> resultType;
    private Class<? extends R> proxiedResultType;
    private final Map<String, Object> primitives = new HashMap<String, Object>();
    private final Map<String, ValueContainer> composites = new HashMap<String, ValueContainer>();
    private final Map<String, EntityContainer<? extends AbstractEntity<?>>> entities = new HashMap<String, EntityContainer<? extends AbstractEntity<?>>>();
    private final Map<String, CollectionContainer<? extends AbstractEntity<?>>> collections = new HashMap<String, CollectionContainer<? extends AbstractEntity<?>>>();
    private boolean instrumented = false;

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

        return (countAllDataItems() == 1 && primitives.containsKey(AbstractEntity.ID) && getId() == null) || (isUnionEntityType(resultType) && countAllDataItems() == 0)
        || (isPersistentEntityType(resultType) || isSyntheticBasedOnPersistentEntityType(resultType)) && primitives.containsKey(AbstractEntity.ID) && primitives.get(AbstractEntity.ID) == null;
    }

    public Long getId() {
        final Object idObject = primitives.get(AbstractEntity.ID);

        if (idObject != null) {
            return ((Number) idObject).longValue();
        } else if (isUnionEntityType(resultType)) {
            final Optional<Long> opId = entities.values().stream()
                    .map(EntityContainer::id)
                    .filter(EntityContainer::idIsNotNull)
                    .findFirst();
            return opId.orElse(null);
        } else {
            return null;
        }
    }

    private static Long id(final EntityContainer<? extends AbstractEntity<?>> container) {
        return container.getId();
    }

    private static boolean idIsNotNull(final Long id) {
        return id != null;
    }

    public EntityContainer<R> mkInstrumented() {
        this.instrumented = true;
        return this;
    }
    
    public boolean isInstrumented() {
        return instrumented;
    }

    public Class<R> getResultType() {
        return resultType;
    }

    public Class<? extends R> getProxiedResultType() {
        return proxiedResultType;
    }

    public void setProxiedResultType(final Class<? extends R> proxiedResultType) {
        this.proxiedResultType = proxiedResultType;
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