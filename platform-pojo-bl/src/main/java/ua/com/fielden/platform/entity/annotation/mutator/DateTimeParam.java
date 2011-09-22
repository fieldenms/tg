package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.joda.time.DateTime;

/**
 * Represents a BCE handler parameter of type Joda DateTime. The actual parameter value is specified as a String representing date/dime in the ISO format and converted to {@link DateTime} upon handler instantiation.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface DateTimeParam {
    String name();
    String value();
}
