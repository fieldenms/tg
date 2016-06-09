package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to indicate if entity's desc property is required by default (i.e. cannot have null value).
 * The {@link Required} annotation can be overwritten by changing property <code>required</code> of a corresponding meta-property instance.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DescRequired {
    String value() default ""; // should be used if description needs to have custom requiredness error message
}
