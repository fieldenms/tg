package ua.com.fielden.platform.entity.query;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.eql.meta.EntityCategory;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata;
import ua.com.fielden.platform.utils.Pair;

public class IdOnlyProxiedEntityTypeCache implements IIdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap;

    public IdOnlyProxiedEntityTypeCache(final EqlDomainMetadata domainMetadata) {
        typesMap = buildMap(domainMetadata);
    }

    @Override
    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap(final EqlDomainMetadata domainMetadata) {
        // the following operations are a bit heave and benefit from parallel processing
        return domainMetadata.entityPropsMetadata().values().parallelStream().filter(md -> md.typeInfo.category == EntityCategory.PERSISTENT)
        .map(entry -> {
            final Class<? extends AbstractEntity<?>> key = entry.entityType;
            final Class<? extends AbstractEntity<?>> proxyType = produceIdOnlyProxiedResultType(key, entry.props());
            return Pair.<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>>pair(key, proxyType);})
        .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private <T extends AbstractEntity<?>> Class<? extends T> produceIdOnlyProxiedResultType(final Class<T> originalType, final Set<EqlPropertyMetadata> propsMetadata) {
        final List<String> proxiedProps = propsMetadata.stream().filter(ppi -> (!ID.equals(ppi.name) && !ppi.isVirtualKey() && !ppi.critOnly)).map(ppi -> ppi.name).toList();
        return EntityProxyContainer.proxy(originalType, proxiedProps.toArray(new String[] {}));
    }
}