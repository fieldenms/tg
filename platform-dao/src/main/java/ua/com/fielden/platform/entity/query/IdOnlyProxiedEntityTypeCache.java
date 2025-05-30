package ua.com.fielden.platform.entity.query;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxyEntity;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;

@Singleton
public class IdOnlyProxiedEntityTypeCache implements IIdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap;

    @Inject
    public IdOnlyProxiedEntityTypeCache(final IDomainMetadataUtils domainMetadataUtils) {
        typesMap = buildMap(domainMetadataUtils);
    }

    @Override
    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap(final IDomainMetadataUtils domainMetadataUtils) {
        // the following operations are a bit heavy and benefit from parallel processing
        return domainMetadataUtils.registeredEntities()
                .parallel()
                .filter(EntityMetadata::isPersistent)
                .map(em -> {
                    final var origType = em.javaType();
                    final var proxyType = produceIdOnlyProxiedResultType(em);
                    return Pair.<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>>pair(origType, proxyType);
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Class<? extends AbstractEntity<?>> produceIdOnlyProxiedResultType(final EntityMetadata entity) {
        final Set<String> proxiedProps = entity.properties().stream()
                .filter(pm -> !ID.equals(pm.name()) && !pm.type().isCompositeKey() && !pm.isCritOnly()
                              && !pm.type().isCollectional()
                              && !(pm.isPlain() && entity.isPersistent()))
                .map(PropertyMetadata::name)
                .collect(toImmutableSet());
        return EntityProxyContainer.proxy(entity.javaType(), proxiedProps, List.of(IIdOnlyProxyEntity.class));
    }
}
