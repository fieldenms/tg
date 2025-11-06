package ua.com.fielden.platform.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotation used to mark methods or property fields that require access authorisation.
/// The annotation parameter must specify the corresponding security token.
///
/// At runtime, methods annotated with this annotation are wrapped by a method interceptor
/// that verifies whether the current user has the required permission to invoke the method.
///
/// For properties, where fields are annotated, the semantics are currently enforced
/// at the level of Entity Centres and GraphQL, controlling the ability to retrieve
/// data for such properties and to use them in selection criteria.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD})
public @interface Authorise {

    Class<? extends ISecurityToken> value();

}
