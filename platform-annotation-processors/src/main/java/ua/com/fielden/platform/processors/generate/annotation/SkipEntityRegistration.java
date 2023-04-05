package ua.com.fielden.platform.processors.generate.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor;

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
