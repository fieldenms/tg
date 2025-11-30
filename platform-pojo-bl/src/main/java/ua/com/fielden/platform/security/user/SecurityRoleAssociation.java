package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;
import ua.com.fielden.platform.utils.ClassComparator;
import ua.com.fielden.platform.utils.Pair;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// Entity that represents the association between the [ISecurityToken] and the [UserRole] entities.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(SecurityRoleAssociationCo.class)
public class SecurityRoleAssociation extends ActivatableAbstractEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(SecurityRoleAssociation.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public static final String
            SECURITY_TOKEN = "securityToken",
            ROLE = "role";

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo
    @PersistentType(userType = ISecurityTokenType.class)
    private Class<? extends ISecurityToken> securityToken;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo
    @SkipEntityExistsValidation
    @Dependent(ACTIVE)
    private UserRole role;

    /// Redefined property `active` to skip the default validation to improve activation/deactivation performance.
    /// This is possible because instances of [SecurityRoleAssociation] are not referenced by any other entities.
    ///
    @IsProperty
    @MapTo
    @Title(value = "Active?", desc = "Designates whether an entity instance is active or not.")
    private boolean active;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    @Observable
    public SecurityRoleAssociation setActive(final boolean active) {
        this.active = active;
        return this;
    }

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
