package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;
import ua.com.fielden.platform.utils.ClassComparator;

/// Entity that represents the association between the [ISecurityToken] and the [UserRole] entities.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(SecurityRoleAssociationCo.class)
public class SecurityRoleAssociation extends AbstractPersistentEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo
    @PersistentType(userType = ISecurityTokenType.class)
    private Class<? extends ISecurityToken> securityToken;

    @IsProperty
    @CompositeKeyMember(2)
    @MapTo
    @SkipEntityExistsValidation
    private UserRole role;

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