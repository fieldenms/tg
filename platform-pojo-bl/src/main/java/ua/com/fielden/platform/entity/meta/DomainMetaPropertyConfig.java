package ua.com.fielden.platform.entity.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a configuration point for instances of {@link IAfterChangeEventHandler} to be associated with entity types and their properties.
 */
public final class DomainMetaPropertyConfig {
    private final Map<Class<?>, Map<String, IAfterChangeEventHandler<?>>> domainMetaDefiners = new HashMap<>();

    /**
     * Return domain validator associated with an entity of the specified type and its property. The returned value is null if no association was found.
     *
     * @param entityType
     * @param propertyName
     * @return
     */
    public IAfterChangeEventHandler<?> getDefiner(final Class<?> entityType, final String propertyName) {
        return domainMetaDefiners.getOrDefault(entityType, Collections.emptyMap()).get(propertyName);
    }

    /**
     * Associates an instance of domain validator with an entity type and its property.
     *
     * @param entityType
     * @param propertyName
     * @param domainMetaDefiner
     * @return
     */
    public DomainMetaPropertyConfig setDefiner(final Class<?> entityType, final String propertyName, final IAfterChangeEventHandler<?> domainMetaDefiner) {
        final Map<String, IAfterChangeEventHandler<?>> map = domainMetaDefiners.computeIfAbsent(entityType, key -> new HashMap<>());
        map.put(propertyName, domainMetaDefiner); // this put replaces a validator if there was already one associated with the specified property
        return this;
    }

}
