package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

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
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity that represents the association between {@link User} and {@link UserRole} entities.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("USER_ROLE_ASSOCIATION")
@CompanionObject(UserAndRoleAssociationCo.class)
public class UserAndRoleAssociation extends AbstractPersistentEntity<DynamicEntityKey> {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(UserAndRoleAssociation.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("ID_CRAFT")
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private User user;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("ID_USER_ROLE")
    @SkipEntityExistsValidation(skipActiveOnly = true)
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