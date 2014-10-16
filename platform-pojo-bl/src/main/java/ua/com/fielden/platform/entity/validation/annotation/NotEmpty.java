package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.validation.NotEmptyValidator;

/**
 * Annotation requiring that a property of type <code>String</code> ({@link Object#toString()} conversion is used if property has some other type) that is accessed by the annotated setter
 * is not empty, but can be <code>null</code>.
 * 
 * <p>
 * A single parameter <code>value</code> for this annotation provides a way to specify a custom error message.
 * <p>
 * Please also refer to the annotation handling logic in class {@link NotEmptyValidator}.
 * 
 * @author TG Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface NotEmpty {
    String value() default "";
}
