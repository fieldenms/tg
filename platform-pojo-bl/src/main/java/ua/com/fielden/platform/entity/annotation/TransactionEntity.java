package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a contract for entities that have "galloping" and most likely immutable nature. These entities appear very quickly and should have adequate representation for the user
 * -- they should appear often with efficient <code>delta</code> mechanism.
 * <p>
 * <code>Delta</code> mechanism should be based on <code>transaction date</code> of the entity, which represents a first moment when the property appears (in terms of server side
 * time).
 * <p>
 * Most likely <code>transaction date</code> property should have a {@link TransactionDate} annotation assigned to provide its automatic server side population.
 * 
 * @author TG Team
 * 
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface TransactionEntity {
    String value() default ""; // represents a property name for "transaction date".
}
