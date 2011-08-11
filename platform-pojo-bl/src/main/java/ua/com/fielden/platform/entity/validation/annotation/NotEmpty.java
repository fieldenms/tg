/**
 *
 */
package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating, that {@link String} property (or property of other type, in this case {@link Object#toString()} method is taken into account), related to annotated
 * setter, should not be empty. As {@link NotNull} annotation this one also have property which is used as error message.
 * 
 * @author Yura
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface NotEmpty {
    String value() default "";
}
