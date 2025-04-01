package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used for specifying mapping of properties to corresponding table columns.
 * 
 * @author TG Team
 */

@Retention(RUNTIME)
@Target({ FIELD })
public @interface MapTo {

    /**
     * Represents an alternative column name.
     * If the value is empty, then the column name is derived based on the predefined rules.
     */
    String value() default "";
    
    /**
     * A default value for a column.
     * It is used for database schema generation.
     */
    String defaultValue() default "";

    /**
     * By default, optimistic locking is applied to all persistent properties.
     * Conflicts are recognised based on the versioning mechanism.
     * And by default, an automatic conflict resolution takes place, which may or may not succeed.
     * <p>
     * There are situations in practice where automatic conflict resolution needs to be avoided.
     * It is possible to do this at the level of entities entirely by annotating them with `@MapEntityTo(autoConflictResolution = false)`.
     * However, such a mechanism is too broad, and having the ability to avoid automatic conflict resolution for specific properties provides a more fine-grained tool.
     * <p>
     * `@MapTo(autoConflictResolution = false)` indicates to the optimistic locking mechanism that if there is a version conflict and the annotated property is dirty,
     * then a conflict error should be reported, without attempting to resolve the conflict.
     */
    boolean autoConflictResolution() default true;

}
