package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to indicate if an entity property is required by default -- cannot have <code>null</code> or empty (in case of string) value.
 * The <code>Required</code> annotation can be overwritten at runtime for entity instances by changing their meta-property information (property <code>required</code> of a corresponding meta-property instance).
 * <p>
 * A custom error message can be provided by specifying value for parameter <code>value()</code>.
 * The message may contain template values <code>{{prop-title}}</code> and <code>{{entity-title}}</code> that would be dynamically replaced with corresponding titles for property and entity at runtime.
 *
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Required {
    String value() default "";
}
