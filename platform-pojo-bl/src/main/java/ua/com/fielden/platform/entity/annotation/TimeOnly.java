package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for properties of type {@link java.util.Date} to convey their semantics as "containing the time portion only".
 * The "date" portion is not truncated or zeroed out automatically -- it is up to the domain logic to handle the "date" portion as deemed appropriate.
 * The UI layer at the platform level, however, does take this annotation into account to display only the time portion.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface TimeOnly {

}
