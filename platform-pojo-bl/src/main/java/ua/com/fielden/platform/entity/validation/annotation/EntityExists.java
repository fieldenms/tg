package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Annotation for indicating setters requiring validation for existence of the passed value, which should be an {@link AbstractEntity} descendant. The meaning of "existence"
 * depends on a concrete implementation of the respective validator.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface EntityExists {
    Class<? extends AbstractEntity<?>> value();

    String errorMsg() default ""; // can be used to provide custom error message
}
