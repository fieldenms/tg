package ua.com.fielden.platform.entity.validation;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * This validator implements a check for the length of a string property.
 *
 * @author TG Air
 *
 */
public class MaxLengthValidator implements IBeforeChangeEventHandler<String> {
    private final Integer limit;

    public MaxLengthValidator(final Integer limit) {
        this.limit = limit;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        final String value = newValue;
        if (StringUtils.isEmpty(value)) { // no violation
            return successful("Value is empty.");
        }

        return value.length() > limit
                ? failure(property.getEntity(), "Value is longer than " + limit + " characters.")
                : successful(property.getEntity());
    }

}
