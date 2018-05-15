package ua.com.fielden.platform.ui.menu;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation on generated entity type (and on criteria entity type) contains information about the entity centre on which the entity type appears.
 * This contains not only menu item type but also 'saveAs' centre configuration name in case of 'named' configurations.
 * 
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface MiType {
    
    /** Describes the menu item type. */
    Class<? extends MiWithConfigurationSupport<?>> value();
    
    /** Contains 'saveAs' name for the centre or empty string for unnamed configurations. */
    String saveAsName() default "";
    
}
