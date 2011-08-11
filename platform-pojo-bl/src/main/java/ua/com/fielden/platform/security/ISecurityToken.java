package ua.com.fielden.platform.security;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * A contract defining method call authorisation token. There can only be one instance of a specific security token however per many user roles. Existence of two instances of the
 * same security token simply does not have any sense, because one instance is responsible for representing a complete information.
 * <p>
 * All security token information related to its relation to the parent token is static in the way that it should be defined at the token class level rather than instance. Thus,
 * token's class hierarchy represents it's hierarchical relation to a parent token.
 * <p>
 * Since no token instance should exist with out a user role, in order to be able to provide an administrative facility for managing user permissions, it is essential to be able to
 * deduce all information about security tokens established for an application by operating reflectively just on token classes.
 * <p>
 * The following simple rules should be followed when implementing a new security token class:
 * <ul>
 * <li>All token classes should either implement ISecurityToken interface or be derived from a class implementing this interface. This information is used for establishing grouping
 * (parent/child) relationships between security tokens. Naturally, tokens directly implementing ISecurityToken are considered to be at the top of groups of security tokens derived
 * from them.
 * <li>Annotation {@link KeyTitle} should be used to annotate each token class to provide some description information about it such as short and long description.
 * </ul>
 * 
 * @author TG Team
 */
public interface ISecurityToken {

}
