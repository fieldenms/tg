package ua.com.fielden.platform.entity.proxy;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IIdOnlyProxiedEntityTypeCache {
    <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType);
}
