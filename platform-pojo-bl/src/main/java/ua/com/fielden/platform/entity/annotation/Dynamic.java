package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.ioc.ObservableMutatorInterceptor;

/**
 * This annotation is "imaginary", so it can't be used to indicate if a setter has the inner validator. But it is used when a setter throws its own Result exception. see
 * {@link ObservableMutatorInterceptor} for more details.
 * 
 * @author Jhou
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Dynamic {
}
