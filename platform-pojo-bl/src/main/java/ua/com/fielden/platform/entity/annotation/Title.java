package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to provide a default title and description (optional) for an entity property, which is most commonly used during UI development. Can be overwritten by changing
 * properties <code>title</code> and <code>desc</code> of a corresponding meta-property.
 *
 * @author 01es
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Title {
    String value() default ""; // represents a default title

    String desc() default ""; // represents a default description
}
