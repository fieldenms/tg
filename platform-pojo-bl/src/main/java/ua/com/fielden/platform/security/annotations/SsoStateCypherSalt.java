package ua.com.fielden.platform.security.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Annotation to mark those constructor arguments that should correspond to environment parameter `auth.sso.state.cypher.salt`.
///
@Retention(RUNTIME)
@Target({PARAMETER})
@BindingAnnotation
public @interface SsoStateCypherSalt {
}