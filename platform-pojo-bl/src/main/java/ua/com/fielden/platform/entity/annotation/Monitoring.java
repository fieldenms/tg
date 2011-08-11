package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.types.ICategorizer;

/**
 * Should be used for indication of fields that are "monitoring" properties. "Monitoring" property changes during its lifecycle and could be used for lifecycle reports.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Monitoring {
    /**
     * This setting should be used to identify a categorizer for this monitoring property.
     * 
     * @return
     */
    Class<? extends ICategorizer> value();
}
