package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to provide a default title and description (optional) for entity, which is most commonly used during UI development.
 * 
 * @author Jhou
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface EntityTitle {
    String value(); // represents a default title

    String desc() default ""; // represents a default description
}
