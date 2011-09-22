package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;

/**
 * Describes BCE handler. Requires handler type to be specified.
 * <p>
 * May provide named parameters if required.
 * All provided parameters are set for the handler instance using field with the name matching the parameter name.
 * Thus, each handler class must have fields for each specified parameter.
 *
 * @author TG Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface Handler {
    Class<? extends IBeforeChangeEventHandler>[] value();
    ClassParam[] clazz() default {};
    IntParam[] integer() default {};
    StrParam[] str() default {};
    DblParam[] dbl() default {};
    DateParam[] date() default {};
    DateTimeParam[] date_time() default {};
    MoneyParam[] money() default {};
}
