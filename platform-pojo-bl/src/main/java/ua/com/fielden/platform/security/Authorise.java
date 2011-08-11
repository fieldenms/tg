package ua.com.fielden.platform.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, which should be used for annotating methods requiring call authorisation. The annotation parameter should be a required security token.
 * <p>
 * During instantiation, methods annotated with this annotation are provided with a method interceptor, which would validate a call permission.
 * 
 * @author 01es
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Authorise {
    Class<? extends ISecurityToken> value(); // represents a security token
}
