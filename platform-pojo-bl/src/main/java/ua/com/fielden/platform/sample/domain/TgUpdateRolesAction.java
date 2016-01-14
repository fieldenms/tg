package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ITgUpdateRolesAction.class)
@KeyType(User.class)
@MapEntityTo
// @KeyTitle(value = "Key", desc = "Some key description")
public class TgUpdateRolesAction extends AbstractFunctionalEntityForCollectionModification<User> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(UserRole.class)
    @Title(value = "A list of applicable roles", desc = "A list of applicable roles")
    private Set<UserRole> roles = new LinkedHashSet<UserRole>();

    @Observable
    protected TgUpdateRolesAction setRoles(final Set<UserRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
        return this;
    }

    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
    
    @Observable
    @Override
    public TgUpdateRolesAction setKey(final User user) {
        super.setKey(user);
        return this;
    }
}