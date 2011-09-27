package ua.com.fielden.platform.criteria.enhanced;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks first parameter of the criteria property.
 * 
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface FirstParam {

    /**
     * Returns the name of the second parameter of the criteria property.
     * 
     * @return
     */
    String secondParam();
}
