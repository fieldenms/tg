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
     * Represents column name
     * 
     * @return
     */
    String value() default "";
    
    String defaultValue() default "";

    int length() default 0;

    int precision() default -1;

    int scale() default -1;
}
