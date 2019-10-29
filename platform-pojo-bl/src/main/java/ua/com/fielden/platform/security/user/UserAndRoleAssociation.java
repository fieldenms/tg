package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

/**
 * Entity that represents the association between {@link User} and {@link UserRole} entities.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("USER_ROLE_ASSOCIATION")
@CompanionObject(IUserAndRoleAssociation.class)
public class UserAndRoleAssociation extends AbstractPersistentEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("ID_CRAFT")
    @SkipEntityExistsValidation
    private User user;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("ID_USER_ROLE")
    @SkipEntityExistsValidation
    private UserRole userRole;

    protected UserAndRoleAssociation() {
        super();
    }

    public User getUser() {
        return user;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    @Observable
    public UserAndRoleAssociation setUser(final User user) {
        this.user = user;
        return this;
    }

    @Observable
    public UserAndRoleAssociation setUserRole(final UserRole userRole) {
        this.userRole = userRole;
        return this;
    }
}