package ua.com.fielden.platform.security.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents a concept of a user role. Multiple users may have the same role. At this stage user role has only key and description.
 * <p>
 * It is also envisaged that multiple roles can be associated with one user. This would provide a flexible facility for configuring user permissions.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle("Role Title")
@DescTitle("Description")
@MapEntityTo("USER_ROLE")
@CompanionObject(IUserRoleDao.class)
public class UserRole extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(value = SecurityRoleAssociation.class, linkProperty = "role")
    @Title(value = "Tokens", desc = "A list of associations between this role and various security tokens.")
    private final Set<SecurityRoleAssociation> tokens = new HashSet<SecurityRoleAssociation>();

    protected UserRole() {
    }

    public UserRole(final String key, final String desc) {
        super(null, key, desc);
    }

    @Observable
    protected UserRole setTokens(final Set<SecurityRoleAssociation> tokens) {
        this.tokens.clear();
        this.tokens.addAll(tokens);
        return this;
    }

    public Set<SecurityRoleAssociation> getTokens() {
        return Collections.unmodifiableSet(tokens);
    }
}
