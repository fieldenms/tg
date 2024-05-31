package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;

public class IdOnlyProxiedEntityTypeCache implements IIdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap;

    public IdOnlyProxiedEntityTypeCache(final IDomainMetadata domainMetadata) {
        typesMap = buildMap(domainMetadata);
    }

    @Override
    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap(final IDomainMetadata domainMetadata) {
        // the following operations are a bit heavy and benefit from parallel processing
        return domainMetadata.allTypes(EntityMetadata.class).parallelStream()
                .filter(em -> em.nature().isPersistent())
                .map(em -> {
                    final Class<? extends AbstractEntity<?>> key = em.javaType();
                    final Class<? extends AbstractEntity<?>> proxyType = produceIdOnlyProxiedResultType(key, em.properties());
                    return Pair.<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>>pair(key, proxyType);
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private <T extends AbstractEntity<?>> Class<? extends T> produceIdOnlyProxiedResultType
            (final Class<T> originalType, final Collection<? extends PropertyMetadata> propsMetadata)
    {
        final List<String> proxiedProps = propsMetadata.stream()
                .filter(pm -> !ID.equals(pm.name()) && !pm.type().isCompositeKey() && !pm.nature().isCritOnly())
                .map(PropertyMetadata::name)
                .toList();
        return EntityProxyContainer.proxy(originalType, proxiedProps);
    }
}
