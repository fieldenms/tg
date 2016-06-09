package ua.com.fielden.platform.security.user;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(IUserRoleTokensUpdater.class)
@MapEntityTo
@KeyTitle(value = "User Role Id", desc = "Id of user role, whose 'tokens' collection modifies by this functional action.")
public class UserRoleTokensUpdater extends AbstractFunctionalEntityForCollectionModification<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(SecurityTokenInfo.class)
    @Title(value = "Security Tokens", desc = "A list of security tokens")
    private Set<SecurityTokenInfo> tokens = new LinkedHashSet<SecurityTokenInfo>();

    @Observable
    protected UserRoleTokensUpdater setTokens(final Set<SecurityTokenInfo> tokens) {
        this.tokens.clear();
        this.tokens.addAll(tokens);
        return this;
    }

    public Set<SecurityTokenInfo> getTokens() {
        return Collections.unmodifiableSet(tokens);
    }
}
