package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.types.Money;

/**
 * Represents a BCE handler parameter of type Money. The actual parameter value is specified as a String and converted to {@link Money} upon handler instantiation.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface MoneyParam {
    String name();

    String value();
}
