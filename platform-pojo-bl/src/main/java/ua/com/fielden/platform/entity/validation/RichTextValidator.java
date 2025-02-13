package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.RichTextSanitiser;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.successful;

/**
 * Default validator for {@link RichText} values.
 * It is implicitly enabled for all {@link RichText} properties.
 * This validator can also be explicitly declared on a property, although the effect will be the same as in its absence.
 * <p>
 * This validator performs HTML sanitisation of formatted text, rejecting values that contain unsafe HTML.
 */
public final class RichTextValidator extends AbstractBeforeChangeEventHandler<RichText> {

    /**
     * Prefix of the error message indicating unsafe HTML.
     * May be used in test assertions.
     */
    public static final String PREFIX_ERR_UNSAFE_INPUT = RichTextSanitiser.STANDARD_ERROR_PREFIX;

    @Override
    public Result handle(
            final MetaProperty<RichText> property,
            final RichText newValue,
            final Set<Annotation> mutatorAnnotations)
    {
        if (newValue == null) {
            return successful();
        }

        return RichTextSanitiser.sanitiseHtml(newValue, RichTextSanitiser.standardErrorFormatter);
    }

}
