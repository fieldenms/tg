package ua.com.fielden.platform.entity.annotation.titles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be used when defining an entity-typed property to override titles for any of the properties of the entity-typed value it references.
 * Property dot-notated paths could be specified to reach as deep into the entity-type hierarchy as needed.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Subtitles {
    PathTitle[] value();
}
