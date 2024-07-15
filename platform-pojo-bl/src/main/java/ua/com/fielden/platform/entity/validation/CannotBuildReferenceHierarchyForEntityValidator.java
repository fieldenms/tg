package ua.com.fielden.platform.entity.validation;

import static ua.com.fielden.platform.error.Result.failure;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * Default restrictive implementation for {@link ICanBuildReferenceHierarchyForEntityValidator}.
 * @author TG Team
 *
 */
public class CannotBuildReferenceHierarchyForEntityValidator implements ICanBuildReferenceHierarchyForEntityValidator {

    @Override
    public Result handle(MetaProperty<String> property, String newValue, Set<Annotation> mutatorAnnotations) {
        return failure("Building reference hiearhcy is restricted by default. More permissive validator is required.");
    }

}
