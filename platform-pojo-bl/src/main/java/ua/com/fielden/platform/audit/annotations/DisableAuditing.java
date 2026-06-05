package ua.com.fielden.platform.audit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Designates a property of an audited entity type as non-auditable.
///
/// Properties with this annotation will not be audited.
///
@Target(FIELD)
@Retention(RUNTIME)
public @interface DisableAuditing {}
