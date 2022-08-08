package ua.com.fielden.platform.annotations.metamodel;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Should be used to annotate entities, which should be recognised as domain entities.
 * All domain entities have a meta-model generated for them.
 * 
 * @author TG Team
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface DomainEntity {

}
