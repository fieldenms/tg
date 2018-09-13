package ua.com.fielden.platform.ui.menu;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used as part of generated entity types (and on criteria entity types) to reify the information about the entity centre on which the entity 
 * type appears: 'saveAs' centre configuration name in case of 'named' configurations. This annotation should not exist for types representing 'unnamed' centres.
 * 
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SaveAsName {
    
    /** Contains non-empty 'saveAs' name for 'named' centre configurations. */
    String value();
    
}
