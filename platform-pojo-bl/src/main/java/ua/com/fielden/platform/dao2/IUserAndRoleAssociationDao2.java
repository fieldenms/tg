package ua.com.fielden.platform.dao2;

import java.util.List;

import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/**
 * Interface that defines the API for retrieving, saving and removing {@link UserAndRoleAssociation} instances
 *
 * @author TG Team
 *
 */
public interface IUserAndRoleAssociationDao2 extends IEntityDao2<UserAndRoleAssociation> {

    /**
     * Removes the list of {@link UserAndRoleAssociation}s from data base
     *
     * @param associations
     */
    void removeAssociation(List<UserAndRoleAssociation> associations);

}
