package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation used for specifying mapping of entities to corresponding tables.
 * 
 * @author TG Team
 */

@Retention(RUNTIME)
@Target({ TYPE })
public @interface MapEntityTo {

    /**
     * Represents a table name where a corresponding entity is mapped to.
     * The default empty value indicates that the table name will be derived automatically from the entity type name.
     *
     * @return
     */
    String value() default "";
    
    /**
     * By default, optimistic locking is applied to all persistent entities.
     * Conflicts are recognised based on the versioning mechanism.
     * And by default, an automatic conflict resolution takes place, which may or may not succeed.
     * <p>
     * However, there are situations in practice where automatic conflict resolution needs to be avoided.
     * Such entities should have attribute {@code autoConflictResolution} set to {@code false}.
     * This will indicate to the optimistic locking mechanism that if there is a version conflict then a conflict error should be reported, without attempting to resolve the source of the conflict.
     *
     * @return
     */
    boolean autoConflictResolution() default true;

}
