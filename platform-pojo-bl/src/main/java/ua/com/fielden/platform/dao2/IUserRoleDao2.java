package ua.com.fielden.platform.dao2;

import java.util.List;

import ua.com.fielden.platform.security.user.UserRole;

/**
 * Interface that defines the API for retrieving and saving {@link UserRole} instances
 *
 * @author TG Team
 *
 */
public interface IUserRoleDao2 extends IEntityDao2<UserRole> {

    /** Returns all available {@link UserRole}s. */
    List<UserRole> findAll();
}
