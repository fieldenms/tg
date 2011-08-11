package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation has an opposite meaning to both {@link CritOnly} and {@link ResultOnly}. Specifically, it indicates that an entity property should be used neither as a criterion
 * nor as a result set value.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Ignore {
}
