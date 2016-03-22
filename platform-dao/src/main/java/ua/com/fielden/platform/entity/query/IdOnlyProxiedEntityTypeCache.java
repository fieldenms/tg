package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;

public class IdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap;

    public IdOnlyProxiedEntityTypeCache(final DomainMetadata domainMetadata) {
        typesMap = buildMap(domainMetadata);
    }

    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap(final DomainMetadata domainMetadata) {
        final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap = new HashMap<>();
        for (final Entry<Class<? extends AbstractEntity<?>>, PersistedEntityMetadata> entityTypeMetadataEntry : domainMetadata.getPersistedEntityMetadataMap().entrySet()) {
            typesMap.put(entityTypeMetadataEntry.getKey(), produceIdOnlyProxiedResultType(entityTypeMetadataEntry.getKey(), entityTypeMetadataEntry.getValue().getProps().values()));
        }
        return typesMap;
    }

    private <T extends AbstractEntity<?>> Class<? extends T> produceIdOnlyProxiedResultType(final Class<T> originalType, final Collection<PropertyMetadata> propsMetadata) {
        final Set<String> proxiedProps = new HashSet<>();
        for (final PropertyMetadata ppi : propsMetadata) {
            final String name = ppi.getName();
            if (!ID.equals(name) &&
                    !(KEY.equals(name) && !ppi.affectsMapping()) &&
                    !ppi.isCollection() &&
                    !name.contains(".") &&
                    !ppi.isSynthetic()) {
                proxiedProps.add(name);
            }
        }
        return EntityProxyContainer.proxy(originalType, proxiedProps.toArray(new String[] {}));
    }
}