package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.validation.NotNullValidator;

/**
 * Annotation for annotating setters to prevent acceptance of <code>null</code> or empty (in case of <code>String</code>) values.
 * <p>
 * A custom error message is supported by assigning the message to attribute <code>value</code>.
 * <p>
 * Please also refer to annotation handling class {@link NotNullValidator} for additional details.
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface NotNull {
    String value() default "";
}
