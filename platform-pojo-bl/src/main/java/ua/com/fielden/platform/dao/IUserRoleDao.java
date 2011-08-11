package ua.com.fielden.platform.dao;

import java.util.List;

import ua.com.fielden.platform.security.user.UserRole;

/**
 * Interface that defines the API for retrieving and saving {@link UserRole} instances
 * 
 * @author TG Team
 * 
 */
public interface IUserRoleDao extends IEntityDao<UserRole> {

    /** Returns all available {@link UserRole}s. */
    List<UserRole> findAll();

    /** Returns user roles matching provided IDs. */
    List<UserRole> findByIds(Long... ids);
}
