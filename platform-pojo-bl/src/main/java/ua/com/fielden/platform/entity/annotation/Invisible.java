package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to indicate if entity property is invisible by default, which is most commonly used during UI development. Can be overwritten by changing properties
 * <code>visible</code> of a corresponding meta-property.
 *
 * @author 01es
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Invisible {
    boolean centreOnly() default false;
}