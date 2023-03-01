package ua.com.fielden.platform.security.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.User;

/**
 * A thread-safe implementation of {@link ISecurityTokenController}.
 *
 * @author TG Team
 *
 */
@Singleton
public class SecurityTokenController implements ISecurityTokenController {

    private final ICompanionObjectFinder coFinder;

    /**
     * Creates new instance of SecurityTokenController with twelve user roles and security tokens
     */
    @Inject
    public SecurityTokenController(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public boolean canAccess(final User user, final Class<? extends ISecurityToken> securityTokenClass) {
        if (securityTokenClass == AlwaysAccessibleToken.class) {
            return true;
        }
        final SecurityRoleAssociationCo coSecurityRoleAssociation = coFinder.find(SecurityRoleAssociation.class, true);
        return coSecurityRoleAssociation.countActiveAssociations(user, securityTokenClass) > 0;
    }

}