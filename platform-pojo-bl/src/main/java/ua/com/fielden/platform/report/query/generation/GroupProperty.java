package ua.com.fielden.platform.report.query.generation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The field annotation that marks the distribution property.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface GroupProperty {

    /**
     * Returns the real group property name.
     *
     * @return
     */
    String groupProperty();
}
