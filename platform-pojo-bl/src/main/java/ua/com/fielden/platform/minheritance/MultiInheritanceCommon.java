package ua.com.fielden.platform.minheritance;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;

public final class MultiInheritanceCommon {

    /// Properties that are excluded from being inherited.
    ///
    public static final Set<String> EXCLUDED_PROPERTIES = ImmutableSet.of(
            KEY, VERSION,
            CREATED_DATE,
            CREATED_BY,
            CREATED_TRANSACTION_GUID,
            LAST_UPDATED_DATE,
            LAST_UPDATED_BY,
            LAST_UPDATED_TRANSACTION_GUID,
            REF_COUNT);


    private MultiInheritanceCommon() {}

}
