/**
 *
 */
package ua.com.fielden.platform.swing.review.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

/**
 * Annotation to be used either on collectional properties or classes that require runtime information about their type parameters. Current it is used for annotating DAO/RAO
 * interfaces to indicate what entity type is managed by them, and also for indicating an intended type on collectional properties in descendants of {@link EntityQueryCriteria}.
 * 
 * @author TG Team
 * 
 */
@Retention(RUNTIME)
@Target({ FIELD, TYPE })
public @interface EntityType {
    /** Describes the main type. */
    Class<? extends AbstractEntity> value();

    /** Describes the main type's parameters if any. */
    Class<?>[] parameters() default {};
}
