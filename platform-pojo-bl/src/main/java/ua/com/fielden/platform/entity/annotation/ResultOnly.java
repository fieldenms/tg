package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation has an opposite to {@link CritOnly} meaning. Specifically, it indicates that an entity property should only be used as part of the result set for dynamic entity
 * reviews (i.e. it cannot be used as one of selection criteria).
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ResultOnly {
}
