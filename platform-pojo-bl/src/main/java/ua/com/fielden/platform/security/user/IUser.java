/**
 *
 */
package ua.com.fielden.platform.security.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * @param passwd
     */
    UserSecret resetPasswd(final User user, final String passwd);
    
    /**
     * Tries to find a user by its password reset UUID.
     * 
     * @param uuid
     * @return
     */
    Optional<User> findUserByResetUuid(final String uuid);
    
    /**
     * Generates a temporal password reset UUID for a user that is identified by the provided username or email address.
     * The generated UUID gets immediately associated with the user and the updated user is returned for further use such as email sending with reset URI.
     * <p>
     * An empty optional value is returned in case where no user was identified by the given username or email address.
     * 
     * @param usernameOrEmail
     * @return
     */
    Optional<UserSecret> assignPasswordResetUuid(final String usernameOrEmail);
    
    /**
     * Returns <code>true</code> if the provided <code>uuid</code> is associated with a user and has not yet expired.
     * 
     * @param uuid
     * @return
     */
    boolean isPasswordResetUuidValid(final String uuid);
    
    /**
     * Estimates password's strength returing <code>true</code> if the presented password is acceptable.
     * 
     * @param passwd
     * @return
     */
    boolean isPasswordStrong(final String passwd);

    /**
     * Locks out the account of {@code username}, which means making a corresponding {@link User} inactive and removing the user's {@code password} and {@code resetUuid}.
     * The {@code username} value may belong to a non-existing user, and this function should still perform gracefully.
     *
     * @param username
     */
    void lockoutUser(final String username);

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
