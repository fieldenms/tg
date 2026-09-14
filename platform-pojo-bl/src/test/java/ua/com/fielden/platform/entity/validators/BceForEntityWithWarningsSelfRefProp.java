package ua.com.fielden.platform.entity.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test_entities.EntityWithWarnings;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.warning;

public class BceForEntityWithWarningsSelfRefProp implements IBeforeChangeEventHandler<EntityWithWarnings> {

    @Override
    public Result handle(MetaProperty<EntityWithWarnings> property, EntityWithWarnings newValue, Set<Annotation> mutatorAnnotations) {
        if (property.getEntity() == newValue) {
            return warning("Self references are discouraged.");
        }
        return Result.successful(newValue);
    }


}
