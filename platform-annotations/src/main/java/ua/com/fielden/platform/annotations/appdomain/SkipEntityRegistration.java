package ua.com.fielden.platform.annotations.appdomain;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation that indicates that an entity type should not be registered with {@code ApplicationDomain}.
 *
 * @see ApplicationDomainProcessor
 */
@Retention(CLASS)
@Target(TYPE)
public @interface SkipEntityRegistration {

}
