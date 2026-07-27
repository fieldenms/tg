package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

import static ua.com.fielden.platform.error.Result.failure;

public final class AuditedEntityInvalidateDefiner implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean value) {
        if (value) {
            property.setDomainValidationResult(failure("Artificial failure"));
        }
    }

}
