package ua.com.fielden.platform.property.validator;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

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
    public static final String validationErrorTemplate = "Value for [%s] in [%s] does not match the pattern.";
    
    protected String regex;

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && !newValue.matches(regex)) {
            return failure(format(validationErrorTemplate, property.getTitle(), getEntityTitleAndDesc(property.getEntity().getType()).getKey()));
        }

        return successful(newValue);
    }

}
