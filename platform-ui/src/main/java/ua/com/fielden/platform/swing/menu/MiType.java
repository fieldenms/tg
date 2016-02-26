package ua.com.fielden.platform.swing.menu;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface MiType {
    /** Describes the menu item type. */
    Class<? extends MiWithConfigurationSupport<?>> value();
}
