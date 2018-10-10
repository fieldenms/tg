package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation, which should be used to indicate that property can accept only values greater or equal to <code>value</code>.
 * It is applicable to properties of type Integer, BigDecimal and Money.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface GreaterOrEqual {
    int value();
}
