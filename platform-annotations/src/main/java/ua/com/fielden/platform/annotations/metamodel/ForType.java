package ua.com.fielden.platform.annotations.metamodel;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Should be used to annotate meta-models with the value equal to the actual type that is being metamodeled.
 * 
 * @author TG Team
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ForType {
    /**
     * @return the underlying type that this meta-model is based on.
     */
    Class<?> value();
}
