package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.IWithValidation;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.RichTextSanitiser;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.successful;

/**
 * Default validator for properties whose type implements {@link IWithValidation}.
 * It is implicitly enabled for all such properties.
 * This validator can also be explicitly declared on a property, although the effect will be the same as in its absence.
 * <p>
 * This validator simply returns the {@linkplain IWithValidation#isValid() validation result} for `newValue` or success if the value is `null`.
 */
public final class DefaultValidatorForValueTypeWithValidation extends AbstractBeforeChangeEventHandler<IWithValidation> {

    @Override
    public Result handle(final MetaProperty<IWithValidation> property, final IWithValidation newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }
        return newValue.isValid();
    }

}
