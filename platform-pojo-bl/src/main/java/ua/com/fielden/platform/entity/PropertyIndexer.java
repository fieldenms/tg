package ua.com.fielden.platform.entity;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;

interface PropertyIndexer {

    Index indexFor(final Class<? extends AbstractEntity<?>> entityType);

    interface Index {
        @Nullable MethodHandle getter(final String prop);

        @Nullable MethodHandle setter(final String prop);

    }

}