package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;
import ua.com.fielden.platform.utils.ClassComparator;

/**
 * Entity that represents the association between the {@link ISecurityToken} and the {@link UserRole} entities.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("SECURITY_ROLE_ASSOCIATION")
@DefaultController(ISecurityRoleAssociationDao.class)
public class SecurityRoleAssociation extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = -4411308363510017782L;

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo(value = "TOKEN", userType = ISecurityTokenType.class)
    private Class<? extends ISecurityToken> securityToken;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("ID_USER_ROLE")
    private UserRole role;

    /**
     * Default constructor.
     */
    protected SecurityRoleAssociation() {
	final DynamicEntityKey key = new DynamicEntityKey(this);
	key.addKeyMemberComparator(1, new ClassComparator());
	setKey(key);
    }

    public Class<? extends ISecurityToken> getSecurityToken() {
	return securityToken;
    }

    @Observable
    public SecurityRoleAssociation setSecurityToken(final Class<? extends ISecurityToken> securityToken) {
	this.securityToken = securityToken;
	return this;
    }

    public UserRole getRole() {
	return role;
    }

    @Observable
    public SecurityRoleAssociation setRole(final UserRole role) {
	this.role = role;
	return this;
    }
}