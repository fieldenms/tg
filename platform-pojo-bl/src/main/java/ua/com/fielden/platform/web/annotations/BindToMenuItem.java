package ua.com.fielden.platform.web.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Should be used to model compound master menu items those need to display the additional information about the presence of related entities.
 *
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface BindToMenuItem {

    /**
     * Should specify the compound master's section title for which annotated property was calculated.
     *
     * @return
     */
    String value();
}
