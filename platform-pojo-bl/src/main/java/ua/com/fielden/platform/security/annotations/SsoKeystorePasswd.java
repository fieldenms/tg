package ua.com.fielden.platform.security.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation to mark those constructor arguments that should correspond to environment parameter {@code auth.sso.keystore.passwd}.
 *
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target({PARAMETER})
@BindingAnnotation
public @interface SsoKeystorePasswd {
}