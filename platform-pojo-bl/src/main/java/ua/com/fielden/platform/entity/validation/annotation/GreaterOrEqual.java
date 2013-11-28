package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation, which should be used to indicate that annotated setter can accept only values greater or equal to <code>value</code>.
 * If applicable to properties of type BigDecimal and Money.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface GreaterOrEqual {
    int value();
}
