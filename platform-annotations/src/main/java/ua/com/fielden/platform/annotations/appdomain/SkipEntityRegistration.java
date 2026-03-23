package ua.com.fielden.platform.annotations.appdomain;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/// Annotates an entity type to indicate that it should not be registered as a domain entity.
/// Such entities are not included in `ApplictionDomain` by the annotation processor.
///
@Retention(CLASS)
@Target(TYPE)
public @interface SkipEntityRegistration {

}
