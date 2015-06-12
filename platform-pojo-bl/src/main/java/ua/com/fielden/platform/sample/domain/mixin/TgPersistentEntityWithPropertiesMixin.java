package ua.com.fielden.platform.sample.domain.mixin;

import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;

/**
 * Mixin implementation for companion object {@link ITgPersistentEntityWithProperties}.
 *
 * @author Developers
 *
 */
public class TgPersistentEntityWithPropertiesMixin {

    private final ITgPersistentEntityWithProperties companion;

    public TgPersistentEntityWithPropertiesMixin(final ITgPersistentEntityWithProperties companion) {
        this.companion = companion;
    }

}