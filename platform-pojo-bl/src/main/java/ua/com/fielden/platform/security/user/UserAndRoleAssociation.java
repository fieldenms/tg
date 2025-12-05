package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.utils.Pair;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// Entity that represents the association between [User] and [UserRole] entities.
///
/// Instances of [UserAndRoleAssociation] get auto-deactivated upon deactivation of [UserRole].
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo("USER_ROLE_ASSOCIATION")
@CompanionObject(UserAndRoleAssociationCo.class)
public class UserAndRoleAssociation extends ActivatableAbstractEntity<DynamicEntityKey> {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(UserAndRoleAssociation.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public static final String
            USER = "user",
            USER_ROLE = "userRole";

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("ID_CRAFT")
    @SkipEntityExistsValidation(skipActiveOnly = true) // This is to allow deactivation of users without having to deactivate all their role associations.
    private User user;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("ID_USER_ROLE")
    private UserRole userRole;

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

    @Observable
    @Override
    public UserAndRoleAssociation setActive(boolean active) {
        super.setActive(active);
        return this;
    }

}
