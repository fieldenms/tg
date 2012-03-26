package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used for specifying mapping of properties to corresponding table columns.
 *
 * @author TG Team
 */

@Retention(RUNTIME)
@Target({ FIELD })
public @interface MapTo {

    /**
     * Represents column name
     * @return
     */
    String value() default "";

    /**
     * Represents result of getName() method on instances of hibernate Type interface implementing classes (e.g. LongType - "long", YesNoType - "yes_no", BigDecimalType - "big_decimal"
     * @return
     */
    String typeName() default ""; //

    /**
     * Returns implementers of IUserTypeInstantiate or ICompositeUserTypeInstantiate (e.g. ISimpleMoneyType.class, ISimplyMoneyWithTaxAmountType.class). Class.class means that nothing has been specified (i.e. Null).
     * @return
     */
    Class userType() default Class.class; // represents hibernate type class

    long length() default 0;

    long precision() default -1;

    long scale() default -1;
}
