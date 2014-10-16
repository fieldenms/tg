package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to indicate if an entity property is required by default (i.e. cannot have <code>null</code> value). 
 * The <code>Required</code> annotation can be overridden by changing property <code>required</code> of a corresponding meta-property instance.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Required {
    String value() default "";
}
