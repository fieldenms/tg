package ua.com.fielden.platform.entity.validation;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * A validator that restricts commas ({@code ','} characters) in a {@code String}-typed property.
 *
 * @author TG Team
 */
public class RestrictCommasValidator implements IBeforeChangeEventHandler<String> {
    public static final String ERR_CONTAINS_COMMAS = "Commas are not permitted.";

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && newValue.contains(",")) {
            return failure(ERR_CONTAINS_COMMAS);
        }

        return successful(newValue);
    }

}