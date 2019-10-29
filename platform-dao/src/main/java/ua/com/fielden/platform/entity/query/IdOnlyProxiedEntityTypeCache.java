package ua.com.fielden.platform.entity.query;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
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
            return Pair.<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>>pair(key, proxyType);})
        .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private <T extends AbstractEntity<?>> Class<? extends T> produceIdOnlyProxiedResultType(final Class<T> originalType, final Collection<PropertyMetadata> propsMetadata) {
        final Set<String> proxiedProps = new HashSet<>();
        for (final PropertyMetadata ppi : propsMetadata) {
            final String name = ppi.getName();
            if (!ID.equals(name) &&
                    !(KEY.equals(name) && !ppi.affectsMapping()) &&
                    !ppi.isCollection() &&
                    !name.contains(".") && // to skip subproperty 'amount' of ISimpleMoneyType properties
                    !ppi.isSynthetic()) {
                proxiedProps.add(name);
            }
        }
        return EntityProxyContainer.proxy(originalType, proxiedProps.toArray(new String[] {}));
    }
}