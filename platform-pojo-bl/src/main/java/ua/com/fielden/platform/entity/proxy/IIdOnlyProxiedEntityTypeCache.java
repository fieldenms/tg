package ua.com.fielden.platform.entity.proxy;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Id-only proxied entity types are cached to avoid multiple creation of such types during server application lifecycle.
 * This interface provides API to get id-only proxy types from original types.
 * 
 * @author TG Team
 *
 */
public interface IIdOnlyProxiedEntityTypeCache {
    /**
     * Returns id-only proxied type for <code>originalType</code>.
     * 
     * @param originalType
     * @return
     */
    <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType);
}
