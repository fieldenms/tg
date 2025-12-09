package ua.com.fielden.platform.processors.minheritance;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Annotates a multi-inheritance entity type to indicate its originating specification entity type.
///
@Target(TYPE)
@Retention(RUNTIME)
public @interface SpecifiedBy {

    /// The specification entity type that was used to generate the annotated entity type.
    ///
    Class<?> value();

}
