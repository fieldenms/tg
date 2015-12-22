package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.DynamicEntityKey;
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
    
    @IsProperty(Integer.class)
    @Title(value = "Chosen roles", desc = "Chosen role numbers")
    private Set<Integer> chosenRoleNumbers = new LinkedHashSet<Integer>();

    @Observable
    protected TgUpdateRolesAction setChosenRoleNumbers(final Set<Integer> chosenRoleNumbers) {
        this.chosenRoleNumbers.clear();
        this.chosenRoleNumbers.addAll(chosenRoleNumbers);
        return this;
    }

    public Set<Integer> getChosenRoleNumbers() {
        return Collections.unmodifiableSet(chosenRoleNumbers);
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