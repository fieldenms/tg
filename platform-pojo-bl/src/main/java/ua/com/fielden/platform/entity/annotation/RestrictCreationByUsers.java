package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that should be used to restrict entity creation with entity master. Entities annotated with this annotation can be created only programmatically.
 *
 * @author TG Team
 */

@Retention(RUNTIME)
@Target(TYPE)
public @interface RestrictCreationByUsers {

}
