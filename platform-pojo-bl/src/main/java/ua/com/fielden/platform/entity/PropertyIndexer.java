package ua.com.fielden.platform.entity;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;

interface PropertyIndexer {

    Index indexFor(final Class<? extends AbstractEntity<?>> entityType);

    interface Index {
        @Nullable MethodHandle accessor(final String prop);

        @Nullable
        PropertyIndexerImpl.PropertySetter setter(final String prop);

    }

}
