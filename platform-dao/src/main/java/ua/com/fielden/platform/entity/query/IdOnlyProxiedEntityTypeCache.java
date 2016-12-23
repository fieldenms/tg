package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.utils.Pair;

public class IdOnlyProxiedEntityTypeCache implements IIdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap;

    public IdOnlyProxiedEntityTypeCache(final DomainMetadata domainMetadata) {
        typesMap = buildMap(domainMetadata);
    }

    @Override
    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap(final DomainMetadata domainMetadata) {
        // the following operations are a bit heave and benefit from parallel processing
        return domainMetadata.getPersistedEntityMetadataMap().entrySet().parallelStream()
        .map(entry -> {
            final Class<? extends AbstractEntity<?>> key = entry.getKey();
            final Class<? extends AbstractEntity<?>> proxyType = produceIdOnlyProxiedResultType(key, entry.getValue().getProps().values());
            return Pair.<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>>pair(key, proxyType);
        })
        .collect(Collectors.toMap(v -> v.getKey(), v -> v.getValue()));
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