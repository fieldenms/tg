package ua.com.fielden.platform.security.user;

import java.util.Comparator;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;

/**
 * Entity that represents the association between the {@link ISecurityToken} and the {@link UserRole} entities.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("SECURITY_ROLE_ASSOCIATION")
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
	key.addKeyMemberComparator(1, new Comparator<Class<?>>() {
	    @Override
	    public int compare(final Class<?> thisClass, final Class<?> thatClass) {
		final String thisValue = thisClass != null ? thisClass.getName() : null;
		final String thatValue = thatClass != null ? thatClass.getName() : null;
		if (thisValue == null && thatValue == null) {
		    return 0;
		}
		if (thisValue != null) {
		    return thisValue.compareTo(thatValue);
		}
		return -1; // if thisValue is null than it is smaller than thatValue
	    }

	});
	setKey(key);
    }

    public Class<? extends ISecurityToken> getSecurityToken() {
	return securityToken;
    }

    @Observable
    public void setSecurityToken(final Class<? extends ISecurityToken> securityToken) {
	this.securityToken = securityToken;
    }

    public UserRole getRole() {
	return role;
    }

    @Observable
    public void setRole(final UserRole role) {
	this.role = role;
    }
}
