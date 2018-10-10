package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;

/**
 * Should be used to declare activatable types that should be deactivated at the same time as the type being annotated.
 * Applicable only to activatable entity types, which is enforced at runtime during entity instantiation.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DeactivatableDependencies {
    Class<? extends ActivatableAbstractEntity<?>>[] value();
}
