package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applicable to entity key members of type {@code String}, indicating that the default string constraints
 * should be relaxed. If {@link #value()} is specified, then only the specified validators are skipped, otherwise all are skipped.
 * <p>
 * For example, the following snippet declares a composite key member {@code title} with specific validators skipped.
 * {@snippet :
@IsProperty
@CompositeKeyMember(1)
@SkipDefaultStringValidation({RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class})
private String title;
 * }
 *
 * @author TG Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SkipDefaultStringKeyMemberValidation {

    Class<? extends IBeforeChangeEventHandler<String>>[] ALL_DEFAULT_STRING_KEY_VALIDATORS = new Class[] {
            RestrictNonPrintableCharactersValidator.class, RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class
    };
    
    Class<? extends IBeforeChangeEventHandler<String>>[] value() default {
        RestrictNonPrintableCharactersValidator.class, RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class
    };

}
