package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining properties that should not be changed once they attain their value (i.e., such properties become immutable).
 * The modus operandi of this annotation is controlled by attributes {@code persistedOnly} and {@code nullIsValueForPersisted}.
 * <p>
 * Attribute {@code persistedOnly} controls when the final semantics are enforced – before or after an entity was persisted.
 * By default the final semantics is enforced only after an entity was persisted (value {@code true}).
 * Otherwise, the final semantics are enforced immediately after assigning a non-{@ code null} value to a property.
 * <p>
 * The default semantics treats {@code null} as not a value.
 * This means that by default immutability is only enforced for properties that have a non-{@code null} value.
 * However, if {@code persistedOnly} is {@code true}, this semantics can be changed in application to persisted entities by setting attribute {@code nullIsValueForPersisted} to {@code true}.
 *
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Final {
    public static final String ERR_REASSIGNMENT = "Changing value for property [%s] in entity %s is not permitted.";

    /** 
     * Controls when the final semantics are enforced -- before or after (default) the assigned property value is persisted.
     * 
     * @return
     */
    boolean persistedOnly() default true;

    /**
     * Controls whether {@code null} should be treated as a value for persisted entities. Thus, restricting assignment even if property has value {@code null}.
     *
     * @return
     */
    boolean nullIsValueForPersisted() default false;
}