package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Validation annotation indicating that annotated property/setter can accept only values less or equal to the value of another, specified property.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface LeProperty {

    /// Property name with which the value of the annotated property will be compared.
    ///
    String[] value();

    /// Lass or equal to comparison is assumed by default.
    /// Set `lt = true` to define strictly less than comparison.
    ///
    /// If several properties are defined in `value`, then `lt` can remain empty (all le) or have the values for each property.
    ///
    boolean[] lt() default {};

}
