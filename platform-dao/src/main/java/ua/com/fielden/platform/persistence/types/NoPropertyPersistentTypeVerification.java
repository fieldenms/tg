package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;

/// Disables verification.
///
public class NoPropertyPersistentTypeVerification implements IPropertyPersistentTypeVerification {

    @Override
    public Result verify(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        return Result.successful();
    }

}
