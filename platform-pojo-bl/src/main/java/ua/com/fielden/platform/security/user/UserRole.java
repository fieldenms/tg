package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.utils.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.user.UserRole.DESC_TITLE;
import static ua.com.fielden.platform.security.user.UserRole.KEY_TITLE;

/**
 * Represents a concept of a user role. Multiple users may have the same role. At this stage user role has only key and description.
 * <p>
 * It is also envisaged that multiple roles can be associated with one user. This would provide a flexible facility for configuring user permissions.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle(KEY_TITLE)
@DescTitle(DESC_TITLE)
@DescRequired
@MapEntityTo("USER_ROLE")
@CompanionObject(UserRoleCo.class)
public class UserRole extends ActivatableAbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(UserRole.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public static final String KEY_TITLE = "Role Title";
    public static final String DESC_TITLE = "Description";

    @IsProperty(value = SecurityRoleAssociation.class, linkProperty = "role")
    @Title(value = "Tokens", desc = "A list of associations between this role and various security tokens.")
    private final Set<SecurityRoleAssociation> tokens = new HashSet<SecurityRoleAssociation>();

    protected  UserRole() {}
    
    @Override
    @Observable
    public UserRole setKey(String key) {
        super.setKey(key);
        return this;
    }
    
    @Override
    @Observable
    public UserRole setDesc(String desc) {
        super.setDesc(desc);
        return this;
    }
    
    @Override
    @Observable
    public UserRole setActive(boolean active) {
        super.setActive(active);
        return this;
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
