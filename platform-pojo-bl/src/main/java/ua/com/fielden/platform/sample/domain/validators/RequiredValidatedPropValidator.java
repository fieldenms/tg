package ua.com.fielden.platform.sample.domain.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import com.google.inject.Inject;

public class RequiredValidatedPropValidator implements IBeforeChangeEventHandler<Integer> {

    @Inject
    public RequiredValidatedPropValidator() {
        super();
    }

    @Override
    public Result handle(final MetaProperty<Integer> property, final Integer newValue, final Integer oldValue, final Set<Annotation> mutatorAnnotations) {
        // final TgPersistentEntityWithProperties ent = (TgPersistentEntityWithProperties) property.getEntity();
        if (newValue > 9999) {
            return Result.failure(newValue, "Over 9999.");
        } else if (newValue > 100) {
            return Result.warning(newValue, "Over 100.");
        } else {
            return Result.successful(newValue);
        }
    }

}