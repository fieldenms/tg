/**
 *
 */
package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;

/**
 * Annotation to be used either on collectional properties or classes that require runtime information about their type parameters. Current it is used for annotating DAO implementations of companion
 * objects to indicate the managed entity type, and also for indicating an intended type on collectional properties in descendants of {@link EntityQueryCriteria}.
 * 
 * @author TG Team
 * 
 */
@Retention(RUNTIME)
@Target({ FIELD, TYPE })
public @interface EntityType {
    /** Describes the main type. */
    Class<? extends AbstractEntity<?>> value();

    /** Describes the main type's parameters if any. */
    Class<?>[] parameters() default {};
}
