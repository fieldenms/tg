package ua.com.fielden.platform.entity.indexer;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.lang.invoke.MethodHandle;

/**
 * A contract for indexers that build {@link PropertyIndex}es to be used for accessing (reading/writing) entity properties.
 */
public interface IPropertyIndexer {

    PropertyIndex indexFor(final Class<? extends AbstractEntity<?>> entityType);

    interface PropertyIndex {
        @Nullable MethodHandle getter(final String prop);

        @Nullable MethodHandle setter(final String prop);

    }

}
