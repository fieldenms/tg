package ua.com.fielden.platform.entity.validation;

import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * This is a super permissive implementation of {@link ICanBuildReferenceHierarchyForEntityValidator}.
 * It should be used with caution... most likely it should not be used for production systems.
 *
 * @author TG Team
 *
 */
public class CanBuildReferenceHierarchyForEveryEntityValidator implements ICanBuildReferenceHierarchyForEntityValidator {

    @Override
    public Result handle(MetaProperty<String> property, String newValue, Set<Annotation> mutatorAnnotations) {
        return successful(newValue);
    }

}
