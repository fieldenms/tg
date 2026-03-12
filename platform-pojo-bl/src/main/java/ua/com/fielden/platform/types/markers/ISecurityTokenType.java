package ua.com.fielden.platform.types.markers;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.security.ISecurityToken;

/// A contract for Hibernate types that map security token types represented by [Class] instances.
///
public interface ISecurityTokenType extends IUserTypeInstantiate {

}
