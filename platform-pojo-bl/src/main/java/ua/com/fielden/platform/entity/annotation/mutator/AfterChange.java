package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.types.Money;

/**
 * An annotation to be used for annotating property's getter in order to process the fact that the property value has changed.
 * <p>
 * Parameter <code>value</code> accepts a class handling the event. Unlike BCE, there can be only one ACE event handler.
 * <p>
 * Similarly to {@link Handler}, named parameters can be provided if required.
 * All provided parameters are set for the handler instance using field with the name matching the parameter name.
 * Thus, each handler class must have fields for each specified parameter.
 * <p>
 * <b>IMPORTANT:</b><i>At this stage this annotation should be used strictly for setters (i.e. collectional property decrementros and incrementros are not supported.)</i>
 *
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface AfterChange {
    /**
     * Class indicating an ACE handler.
     *
     * @return
     */
    Class<? extends IAfterChangeEventHandler> value();

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
