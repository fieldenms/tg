package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.RichTextSanitiser;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failureEx;
import static ua.com.fielden.platform.error.Result.successful;

/**
 * A default validator for properties of type {@link String} to prevent injection of dangerous JS code, used for XSS.
 * Every non-calculated property with `length > 0` attains this validator automatically.
 * This validator can also be explicitly declared on a property, although the effect will be the same as in its absence.
 */
public class SanitiseHtmlValidator implements IBeforeChangeEventHandler<String> {

    @Override
    public Result handle(final MetaProperty<String> mp, final String newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        return RichTextSanitiser.sanitiseHtml(newValue);
    }

}
