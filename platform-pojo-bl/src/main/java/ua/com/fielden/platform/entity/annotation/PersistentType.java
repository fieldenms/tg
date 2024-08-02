package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used for specifying mapping of properties to corresponding table columns.
 * <p>
 * Either none or only one of {@link #value()} and {@link #userType()} should be specified.
 * 
 * @author TG Team
 */
@Retention(RUNTIME)
@Target({ FIELD })
public @interface PersistentType {

    /**
     * Name of a Hibernate Type that should be used to map the annotated property's type.
     * <p>
     * The name is interpreted as if returned by {@link org.hibernate.type.Type#getName()} (e.g. LongType - "long",
     * YesNoType - "yes_no", BigDecimalType - "big_decimal").
     * <p>
     * Empty string indicates value absence.
     */
    String value() default "";

    /**
     * Type that implements {@link IUserTypeInstantiate} or {@link ICompositeUserTypeInstantiate}, and will be used to
     * map the annotated property's type.
     * <p>
     * {@link Void} indicates value absence.
     */
    Class userType() default Void.class;

}
