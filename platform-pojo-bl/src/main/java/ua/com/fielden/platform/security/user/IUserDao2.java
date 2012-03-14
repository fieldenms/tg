/**
 *
 */
package ua.com.fielden.platform.security.user;

import java.util.List;

import ua.com.fielden.platform.dao2.IEntityDao2;

/**
 * Contract for DAO handling user instances.
 *
 * @author TG Team
 *
 */
public interface IUserDao2 extends IEntityDao2<User> {

    /**
     * Returns users with roles.
     *
     * @return
     */
    List<User> findAllUsersWithRoles();

    /**
     * Returns the users without their roles.
     *
     * @return
     */
    List<User> findAllUsers();

    /**
     * Returns a user with the specified id. The returned user is with initialised set of roles.
     *
     * @param id
     * @return
     */
    User findUserByIdWithRoles(Long id);

    /**
     * Returns the user with specified key. The returned user also have associated user roles.
     *
     * @param key
     * @return
     */
    User findUserByKeyWithRoles(String key);

}
