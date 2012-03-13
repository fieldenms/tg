package ua.com.fielden.platform.dao2;

import java.util.List;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;

/**
 * Interface that defines the API for retrieving saving and removing the {@link SecurityRoleAssociation} instances.
 *
 * @author TG Team
 *
 */
public interface ISecurityRoleAssociationDao2 extends IEntityDao2<SecurityRoleAssociation> {

    /**
     * Returns the list of {@link SecurityRoleAssociation} those are associated with given security token
     *
     * @param securityToken
     * @return
     */
    List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /**
     * Removes all role association with the specified token.
     *
     * @param association
     */
    void removeAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /** Returns a total number of user roles associated with the token. */
    int countAssociations(String username, final Class<? extends ISecurityToken> securityTokenClass);
}
