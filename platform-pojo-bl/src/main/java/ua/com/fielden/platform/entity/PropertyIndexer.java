package ua.com.fielden.platform.entity;

import javax.annotation.Nullable;

interface PropertyIndexer {

    Index indexFor(final Class<? extends AbstractEntity<?>> entityType);

    interface Index {
        @Nullable PropertyIndexerImpl.PropertyAccessor accessor(final String prop);

        @Nullable
        PropertyIndexerImpl.PropertySetter setter(final String prop);

    }

}
