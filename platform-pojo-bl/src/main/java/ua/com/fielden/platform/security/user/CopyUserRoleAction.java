package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

@CompanionObject(CopyUserRoleActionCo.class)
@KeyType(NoKey.class)
public class CopyUserRoleAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public static final String
            ROLE_TITLE = "roleTitle",
            ROLE_DESC = "roleDesc",
            ROLE_ACTIVE = "roleActive";

    protected CopyUserRoleAction() {
        setKey(NO_KEY);
    }

    @IsProperty(Long.class)
    @Title(value = "Selected User Role IDs")
    private final Set<Long> selectedIds = new HashSet<>();

    @IsProperty
    @Required
    @Title(value = UserRole.KEY_TITLE)
    private String roleTitle;

    @IsProperty
    @Required
    @Title(value = UserRole.DESC_TITLE)
    private String roleDesc;

    @IsProperty
    @Title(value = "Active?")
    private boolean roleActive;

    public boolean isRoleActive() {
        return roleActive;
    }

    @Observable
    public CopyUserRoleAction setRoleActive(final boolean roleActive) {
        this.roleActive = roleActive;
        return this;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    @Observable
    public CopyUserRoleAction setRoleDesc(final String roleDesc) {
        this.roleDesc = roleDesc;
        return this;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    @Observable
    public CopyUserRoleAction setRoleTitle(final String roleTitle) {
        this.roleTitle = roleTitle;
        return this;
    }

    public Set<Long> getSelectedIds() {
        return unmodifiableSet(selectedIds);
    }

    @Observable
    public CopyUserRoleAction setSelectedIds(final Set<Long> selectedIds) {
        this.selectedIds.clear();
        this.selectedIds.addAll(selectedIds);
        return this;
    }

}
