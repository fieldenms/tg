package ua.com.fielden.platform.entity.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;

@Retention(RUNTIME)
@Target({ FIELD })
public @interface MasterEntityType {
    /** Describes the master type. */
    Class<? extends AbstractEntity<?>> value();
}
