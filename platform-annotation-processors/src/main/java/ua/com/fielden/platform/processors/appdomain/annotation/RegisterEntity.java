package ua.com.fielden.platform.processors.appdomain.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;

/**
 * An annotation that should be used in conjunction with {@link ExtendApplicationDomain} to register additional domain entity types.
 *
 * @see ApplicationDomainProcessor
 *
 * @author TG Team
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface RegisterEntity {

    Class<? extends AbstractEntity<?>> value();

}
