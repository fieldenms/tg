package ua.com.fielden.platform.entity.meta.test_entities.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithDependentProperties;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

public class EntityWithDependentPropertiesOne implements IBeforeChangeEventHandler<String> {

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, Set<Annotation> mutatorAnnotations) {
        ((EntityWithDependentProperties) property.getEntity()).oneCount++;
        return Result.successful(newValue);
    }

}
