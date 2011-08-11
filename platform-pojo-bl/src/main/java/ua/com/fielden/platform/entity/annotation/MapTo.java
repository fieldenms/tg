package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used for specifying mapping of properties to corresponding table columns.
 *
 * @author TG Team
 */

@Retention(RUNTIME)
@Target({ FIELD })
public @interface MapTo {

    String value() default ""; // represents column name

    String typeName() default ""; // represents hibernate type class name

    Class userType() default Class.class; // represents hibernate type class
}
