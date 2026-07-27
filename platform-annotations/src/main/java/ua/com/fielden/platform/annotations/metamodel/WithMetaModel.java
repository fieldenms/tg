package ua.com.fielden.platform.annotations.metamodel;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Use to annotate entities, which may not be necessarily [DomainEntity], but do need their meta-models generated.
/// The actual meta-models generated for domain entities and entities annotated with this annotation, may diverge over time.
///
@Retention(RUNTIME)
@Target(TYPE)
public @interface WithMetaModel {

}
