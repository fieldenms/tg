package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.UserRole;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgUpdateRolesAction.class)
@KeyType(String.class)
public class TgUpdateRolesAction extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(UserRole.class)
    @Title(value = "A list of applicable roles", desc = "A list of applicable roles")
    private Set<UserRole> roles = new LinkedHashSet<UserRole>();
    
    @IsProperty(Long.class) // TODO investigate the type parameter 
    @Title(value = "Chosen roles", desc = "Chosen role numbers")
    private Map<Long, Boolean> chosenRoleIds = new LinkedHashMap<Long, Boolean>();
    
    @IsProperty(Long.class)
    @Title(value = "Removed role ids", desc = "IDs of removed roles")
    private Set<Long> removedRoleIds = new LinkedHashSet<Long>();

    @Observable
    protected TgUpdateRolesAction setRemovedRoleIds(final Set<Long> removedRoleIds) {
        this.removedRoleIds.clear();
        this.removedRoleIds.addAll(removedRoleIds);
        return this;
    }

    public Set<Long> getRemovedRoleIds() {
        return Collections.unmodifiableSet(removedRoleIds);
    }

    @Observable
    protected TgUpdateRolesAction setChosenRoleIds(final Map<Long, Boolean> chosenRoleIds) {
        this.chosenRoleIds.clear();
        this.chosenRoleIds.putAll(chosenRoleIds);
        return this;
    }

    public Map<Long, Boolean> getChosenRoleIds() {
        return Collections.unmodifiableMap(chosenRoleIds);
    }

    @Observable
    protected TgUpdateRolesAction setRoles(final Set<UserRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
        return this;
    }

    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}