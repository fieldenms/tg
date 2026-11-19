package ua.com.fielden.platform.audit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Designates a property of an audited entity type as non-auditable.
///
/// Properties with this annotation will not be audited.
///
/// This concerns what is recorded, and is independent of `@Invisible`, which concerns what clients receive:
/// the values of an invisible property are audited, unless it is annotated with `@DisableAuditing` as well.
///
@Target(FIELD)
@Retention(RUNTIME)
public @interface DisableAuditing {}
