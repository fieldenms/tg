package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * A validator that restricts commas ({@code ','} characters) in a {@code String}-typed property.
 *
 * @author homedirectory
 */
public class RestrictCommasValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_COMMAS = "Value contains commas.";

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && newValue.contains(",")) {
            return Result.failure(ERR_CONTAINS_COMMAS);
        }

        return Result.successful(newValue);
    }

}