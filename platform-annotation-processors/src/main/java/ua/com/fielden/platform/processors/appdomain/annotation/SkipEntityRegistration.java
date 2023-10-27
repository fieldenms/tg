package ua.com.fielden.platform.processors.appdomain.annotation;

import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotation that indicates that an entity type should not be registered with {@code ApplicationDomain}.
 *
 * @see ApplicationDomainProcessor
 *
 * @author TG Team
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface SkipEntityRegistration {

}
