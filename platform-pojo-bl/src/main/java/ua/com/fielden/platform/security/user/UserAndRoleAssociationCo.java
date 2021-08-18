package ua.com.fielden.platform.security.user;

import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Interface that defines the API for retrieving, saving and removing {@link UserAndRoleAssociation} instances
 * 
 * @author TG Team
 * 
 */
public interface UserAndRoleAssociationCo extends IEntityDao<UserAndRoleAssociation> {

    /**
     * Removes the list of {@link UserAndRoleAssociation}s from data base
     * 
     * @param associations
     */
    void removeAssociation(Set<UserAndRoleAssociation> associations);

}
