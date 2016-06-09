package ua.com.fielden.platform.property.validator;

import static java.lang.String.*;
import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Can be used to validate any property of type string based on a regular expression.
 *
 * @author TG Team
 *
 */
public class StringValidator implements IBeforeChangeEventHandler<String> {

    public static final String regexProp = "regex";
    public static final String validationErrorTemplate = "New value [%s] for property [%s] in entity [%s] does not pass pattern validation.";
    
    private String regex;

    public StringValidator() {
    }

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final String oldValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && !newValue.matches(regex)) {
            return Result.failure(format(validationErrorTemplate, newValue, property.getName(), property.getEntity().getType().getSimpleName()));
        }

        return Result.successful(newValue);
    }

}
