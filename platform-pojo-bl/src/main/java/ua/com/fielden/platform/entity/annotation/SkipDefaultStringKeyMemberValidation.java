package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator;

/**
 * This annotation is applicable to entity key members of type {@code String}, which should have the default string constraints relaxed.
 * Attribute {@code value} can be assigned when annotating a key-member field to skip specific validators. Otherwise, all default string validators would be skipped.
 * <p>
 * For example, the following snippet declares a composite key member {@code title} with validators {@code RestrictExtraWhitespaceValidator} and {@code RestrictCommasValidator} skipped.
 * <pre>
 *   &#64;IsProperty
 *   &#64;CompositeKeyMember(1)
 *   &#64;Title(value = "Title", desc = "Calculated property title")
 *   &#64;SkipDefaultStringValidation({RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class})
 *   private String title;
 * </pre>
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SkipDefaultStringKeyMemberValidation {

    static final Class<? extends IBeforeChangeEventHandler<String>>[] ALL_DEFAULT_STRING_KEY_VALIDATORS = new Class[]{RestrictNonPrintableCharactersValidator.class, RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class};
    
    Class<? extends IBeforeChangeEventHandler<String>>[] value() default {RestrictNonPrintableCharactersValidator.class, RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class};

}