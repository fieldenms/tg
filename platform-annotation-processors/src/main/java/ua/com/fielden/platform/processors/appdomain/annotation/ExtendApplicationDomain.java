package ua.com.fielden.platform.processors.appdomain.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;

/**
 * Should be used to annotate a type (<i>extension point</i>) that provides additional information for the generation of {@code ApplicationDomain}.
 * <p>
 * <b>At most one extension point is allowed.</b>
 *
 *
 * @see ApplicationDomainProcessor
 *
 * @author TG Team
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface ExtendApplicationDomain {

    /**
     * A list of additional entity types that should be registered.
     */
    RegisterEntity[] entities() default {};

}
