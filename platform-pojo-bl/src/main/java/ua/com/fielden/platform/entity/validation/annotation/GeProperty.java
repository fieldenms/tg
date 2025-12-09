package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Validation annotation indicating that annotated property/setter can accept only values greater or equal to the value of another, specified property.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface GeProperty {

    /// Property name with which the value of the annotated property will be compared.
    ///
    String[] value();

    /// Greater or equal than comparison is assumed by default.
    /// Set `gt = true` to define strictly greater than comparison.
    ///
    /// If several properties are defined in `value`, then `gt` can remain either empty (all ge) or have values for each property.
    ///
    boolean[] gt() default {};

}
