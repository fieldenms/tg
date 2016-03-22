package ua.com.fielden.platform.security.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Extended {@link User} companion that provides API for retrieving and saving user roles associated and users.
 *
 * @author TG Team
 *
 */
public interface IUserEx extends IUser {

    /**
     * Returns all available user roles
     *
     * @return
     */
    List<? extends UserRole> findAllUserRoles();

    /**
     * Returns the first page of users fetched with user roles. The page will have less or equal number of items specified by the capacity parameter.
     *
     * @param capacity
     *            - the number of users in the page. (Notice that page may have less then the value specified by the capacity parameter).
     * @return
     */
    IPage<? extends User> firstPageOfUsersWithRoles(int capacity);

    /**
     * Returns the list of users. Users must be associated with user roles.
     *
     * @return
     */
    @Override
    List<User> findAllUsers();

    /**
     * Updates association between user and a list of roles. Once completer user should be associated strictly with the specified list of roles.
     *
     * @param user
     * @param userRoles
     */
    void updateUsers(Map<User, Set<UserRole>> userRolesMap);

    /**
     * Finds user by name.
     *
     * @param username
     * @return
     */
    User findUser(String username);

}
