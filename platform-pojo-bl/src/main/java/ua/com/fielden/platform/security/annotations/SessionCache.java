package ua.com.fielden.platform.security.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.common.cache.Cache;
import com.google.inject.BindingAnnotation;

/**
 * Annotations to mark those constructor arguments representing a session cache of type {@link Cache}.
 *
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target({ElementType.PARAMETER})
@BindingAnnotation
public @interface SessionCache {
}