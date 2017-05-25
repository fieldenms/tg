package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation used for specifying mapping of entities to corresponding tables.
 * 
 * @author TG Team
 */

@Retention(RUNTIME)
@Target({ TYPE })
public @interface MapEntityTo {

    String value() default ""; // represents table name

}
