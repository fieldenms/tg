package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity that represents the association between {@link User} and {@link UserRole} entities.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("USER_ROLE_ASSOCIATION")
public class UserAndRoleAssociation extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("ID_CRAFT")
    private User user;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("ID_USER_ROLE")
    private UserRole userRole;

    /**
     * Hibernate constructor
     */
    protected UserAndRoleAssociation() {
	setKey(new DynamicEntityKey(this));
    }

    /**
     * Custom constructor
     *
     * @param user
     * @param userRole
     */
    public UserAndRoleAssociation(final User user, final UserRole userRole) {
	setKey(new DynamicEntityKey(this));
	setUser(user);
	setUserRole(userRole);
    }

    public User getUser() {
	return user;
    }

    public UserRole getUserRole() {
	return userRole;
    }

    @Observable
    public void setUser(final User user) {
	this.user = user;
    }

    @Observable
    public void setUserRole(final UserRole userRole) {
	this.userRole = userRole;
    }

}
