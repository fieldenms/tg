package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.types.Money;

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
    /**
     * Class indicating an BCE handler.
     *
     * @return
     */
    Class<? extends IBeforeChangeEventHandler> value();
    /**
     * Should be used for specifying non-ordinary parameters of the handler. All listed classes should be suitable for instantiation with an injector.
     *
     * @return
     */
    ClassParam[] non_ordinary() default {};

    /**
     * Should be used for specifying parameters of the handler, which are classes and should be assigned as types -- not instances.
     *
     * @return
     */
    ClassParam[] clazz() default {};

    /**
     * Should be used for specifying integer parameters of the handler.
     *
     * @return
     */
    IntParam[] integer() default {};

    /**
     * Should be used for specifying string parameters of the handler.
     *
     * @return
     */
    StrParam[] str() default {};

    /**
     * Should be used for specifying double parameters of the handler.
     *
     * @return
     */
    DblParam[] dbl() default {};

    /**
     * Should be used for specifying {@link Date} parameters of the handler.
     *
     * @return
     */
    DateParam[] date() default {};

    /**
     * Should be used for specifying {@link DateTime} parameters of the handler.
     *
     * @return
     */
    DateTimeParam[] date_time() default {};

    /**
     * Should be used for specifying {@link Money} parameters of the handler.
     *
     * @return
     */
    MoneyParam[] money() default {};
}
