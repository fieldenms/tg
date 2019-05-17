package ua.com.fielden.platform.entity.meta.test_entities.validators;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithCircularDependentPropertiesAndDefiners;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Demand that property {@code one} is assigned before property {@code three} can be assigned.
 *
 * @author TG Team
 *
 */
public class EntityWithCircularDependentPropertiesAndDefiners_PropTree_Validator implements IBeforeChangeEventHandler<String> {

    @Override
    public Result handle(MetaProperty<String> property, String newValue, Set<Annotation> mutatorAnnotations) {
        final EntityWithCircularDependentPropertiesAndDefiners entity = (EntityWithCircularDependentPropertiesAndDefiners) property.getEntity();
        if (entity.getOne() == null) {
            return failure("Cannot assign anything without prop One being populated.");
        }
        return successful(entity);
    }

}
