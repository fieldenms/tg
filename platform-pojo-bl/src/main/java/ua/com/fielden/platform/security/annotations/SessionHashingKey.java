package ua.com.fielden.platform.security.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotations to mark those constructor arguments that require a hashing key that is used for hashing user session data such as session ID and authenticator.
 *
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target({ElementType.PARAMETER})
@BindingAnnotation
public @interface SessionHashingKey {
}