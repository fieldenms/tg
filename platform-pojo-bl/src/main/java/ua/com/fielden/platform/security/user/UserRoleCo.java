package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;

import java.util.List;

/**
 * Interface that defines the API for retrieving and saving {@link UserRole} instances
 * 
 * @author TG Team
 * 
 */
public interface UserRoleCo extends IEntityDao<UserRole>, ISaveWithFetch<UserRole> {

    /** Returns all available {@link UserRole}s. */
    List<UserRole> findAll();

    /** Returns user roles matching provided IDs. */
    List<UserRole> findByIds(Long... ids);

}
