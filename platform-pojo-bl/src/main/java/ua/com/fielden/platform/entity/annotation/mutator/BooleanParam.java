package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a BCE handler parameter of type boolean.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface BooleanParam {
    String name();

    boolean value();
}
