package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Validator for centre configuration title in {@link CentreConfigCopyAction}.
 * 
 * @author TG Team
 *
 */
public class CentreConfigCopyActionTitleValidator implements IBeforeChangeEventHandler<String> {
    
    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && (newValue.contains("[") || newValue.contains("]"))) {
            return failure("Brackets are not allowed.");
        } else if (newValue != null && (newValue.contains("{") || newValue.contains("}"))) {
            return failure("Curly braces are not allowed.");
        }
        return successful("ok");
    }
    
}
