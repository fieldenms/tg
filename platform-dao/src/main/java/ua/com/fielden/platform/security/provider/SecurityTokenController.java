package ua.com.fielden.platform.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A thread-safe implementation of {@link ISecurityTokenController}.
 *
 * @author TG Team
 *
 */
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
    public List<UserRole> findUserRolesFor(final Class<? extends ISecurityToken> securityTokenClass) {
        final SecurityRoleAssociationCo coSecurityRoleAssociation = coFinder.find(SecurityRoleAssociation.class);
        final List<UserRole> roles = new ArrayList<>();
        for (final SecurityRoleAssociation association : coSecurityRoleAssociation.findAssociationsFor(securityTokenClass)) {
            roles.add(association.getRole());
        }
        return roles;
    }

    @Override
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {
        final SecurityRoleAssociationCo coSecurityRoleAssociation = coFinder.find(SecurityRoleAssociation.class);
        return coSecurityRoleAssociation.findAllAssociations();
    }

    @Override
    public boolean canAccess(final User user, final Class<? extends ISecurityToken> securityTokenClass) {
        if (securityTokenClass == AlwaysAccessibleToken.class) {
            return true;
        }
        final SecurityRoleAssociationCo coSecurityRoleAssociation = coFinder.find(SecurityRoleAssociation.class);
        return coSecurityRoleAssociation.countActiveAssociations(user, securityTokenClass) > 0;
    }

}