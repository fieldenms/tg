package ua.com.fielden.platform.entity.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.EntityWithWarnings;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;

public class BceForEntityWithWarningsSelfRefProp implements IBeforeChangeEventHandler<EntityWithWarnings> {

    @Override
    public Result handle(MetaProperty<EntityWithWarnings> property, EntityWithWarnings newValue, Set<Annotation> mutatorAnnotations) {
        if (property.getEntity() == newValue) {
            return new Warning("Self references are discouraged.");
        }
        return Result.successful(newValue);
    }


}
