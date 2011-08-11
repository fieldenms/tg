package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * Should be used to indicate if an entity property is required by default (i.e. cannot can null value). There is also annotation {@link NotNull} used on setters, however it is
 * more restrictive. The <code>Required</code> annotation can be overwritten by changing property <code>required</code> of a corresponding meta-property instance.
 * 
 * @author 01es
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Required {
}
