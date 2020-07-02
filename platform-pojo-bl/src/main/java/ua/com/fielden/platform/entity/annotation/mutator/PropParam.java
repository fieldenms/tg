package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a parameter for BCE and ACE handlers that references a property name of the same entity as the property with the corresponding BCE or ACE handler.
 * <p>
 * The actual semantics of how the referenced property is used depends on a specific handler implementation.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface PropParam {
    /**
     * The name of a BCE/ACE field where the value of the specified property gets assigned to, which must be of type {@code String}.
     * @return
     */
    String name();

    /**
     * The name of a property that is assigned to the specified BCE/ACE field.
     * @return
     */
    String propName();
}
