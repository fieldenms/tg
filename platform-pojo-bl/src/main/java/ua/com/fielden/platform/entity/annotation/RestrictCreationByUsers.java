package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that should be used to restrict instantiation of entities via their Entity Masters.
 * Entity types annotated with this annotation can be created only programmatically.
 * <p>
 * Please note that this annotation get inherited. And so, if it is present on a base entity type, it would also apply to entities extending it.
 * <p>
 * A custom error message can be provided by specifying value for parameter <code>value()</code>.
 * The message may contain template value {@code {{entity-title}}} that would be dynamically replaced with a corresponding entity title at runtime.
 *
 * @author TG Team
 */

@Retention(RUNTIME)
@Target(TYPE)
public @interface RestrictCreationByUsers {

    String value() default "New instances of entity [{{entity-title}}] can not be created via UI as this entity is annotated with @RestrictCreationByUsers.";

}
