package ua.com.fielden.platform.swing.review.report.centre;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks criteria properties.
 * 
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface CriteriaProperty {

    /**
     * Returns original root type for generated criteria property.
     * 
     * @return
     */
    Class<?> rootType();

    /**
     * Returns the property name for which criteria property was generated.
     * 
     * @return
     */
    String propertyName();
}
