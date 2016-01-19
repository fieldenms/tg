package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.UserRole;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ITgUpdateTokensAction.class)
@KeyType(UserRole.class)
@MapEntityTo
@KeyTitle(value = "User Role", desc = "User role, whose 'tokens' collection modifies by this functional action.")
public class TgUpdateTokensAction extends AbstractFunctionalEntityForCollectionModification<UserRole> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(TgSecurityToken.class)
    @Title(value = "A list of applicable tokens", desc = "A list of applicable tokens")
    private Set<TgSecurityToken> tokens = new LinkedHashSet<TgSecurityToken>();

    @Observable
    protected TgUpdateTokensAction setTokens(final Set<TgSecurityToken> tokens) {
        this.tokens.clear();
        this.tokens.addAll(tokens);
        return this;
    }

    public Set<TgSecurityToken> getTokens() {
        return Collections.unmodifiableSet(tokens);
    }
}