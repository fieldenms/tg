package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation, which should be used to indicate the maximum value of a property. This annotation is applicable to numeric and string properties. In case of string
 * properties, it is recognised as the maximum length.
 * 
 * @author 01es
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Max {
    int value();
}
