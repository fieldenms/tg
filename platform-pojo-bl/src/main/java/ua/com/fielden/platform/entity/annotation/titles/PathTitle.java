package ua.com.fielden.platform.entity.annotation.titles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining titles and descriptions for path-dependent properties.
 * Effectively, this is where the title of a property depends on its context -- what is its parent's parent, etc.
 * If specified, the title for property that is specified at its parent entity level is overridden the path-dependent title.  
 *
 * @author TG Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface PathTitle {
    String path(); // dot-notated property path, assumes that the entity where annotation is specified is the root
    
    String title();

    String desc() default ""; // represents a default description

}
