/**
 *
 */
package ua.com.fielden.platform.security.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.pagination.IPage;

/**
 * Contract for DAO handling user instances.
 * 
 * @author TG Team
 * 
 */
public interface IUser extends IEntityDao<User> {

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

    /**
     * Resets the user password.
     * 
     * @param user
     */
    User resetPasswd(final User user);
    
    /**
     * Estimates password's strength returing <code>true</code> if the presented password is acceptable.
     * 
     * @param passwd
     * @return
     */
    boolean isPasswordStrong(final String passwd);
    
    /**
     * A method for hashing the user password before storing it into the database.
     * 
     * @param passwd
     * @param salt
     * @return
     */
    default String hashPasswd(final String passwd, final String salt) throws Exception {
        throw new UnsupportedOperationException();
    }
    
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
