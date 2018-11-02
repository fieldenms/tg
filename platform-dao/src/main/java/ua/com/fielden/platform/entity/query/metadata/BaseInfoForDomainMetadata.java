package ua.com.fielden.platform.entity.query.metadata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public class BaseInfoForDomainMetadata {
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityTypeInfo<?>> map = new ConcurrentHashMap<>();

    public <ET extends AbstractEntity<?>> EntityTypeInfo<ET> getEntityTypeInfo(final Class<ET> entityType) {
        final EntityTypeInfo<ET> existing = (EntityTypeInfo<ET>) map.get(entityType);
        if (existing != null) {
            return existing;
        } else {
            final EntityTypeInfo<ET> created = new EntityTypeInfo<>(entityType);
            map.put(entityType, created);
            return created;
        }
    }
}