package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Annotation for indicating setters requiring validation for existence of the passed value, which should be an {@link AbstractEntity} descendant.
 *
 * The explicit use of this annotation is discouraged due to automatic nature of entity exists validation that is performed regardless of whether this annotation present or not.
 *
 * The notion of entity existence includes the fact that entity is persisted and, in case of activatable entities, is active.
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
