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

    /**
     * Represents an alternative column name. If the value is empty then the column name is derived based on the predefined rules.
     * 
     * @return
     */
    String value() default "";
    
    /**
     * A default value for a column. It is used for database schema generation.
     * 
     * @return
     */
    String defaultValue() default "";
}
