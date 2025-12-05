/**
 *
 */
package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.security.user.User.*;

/// Contract for DAO handling user instances.
///
public interface IUser extends IEntityDao<User> {

    IFetchProvider<User> FETCH_PROVIDER = EntityUtils.fetch(User.class)
            .with(KEY, EMAIL, ACTIVE, SSO_ONLY)
            .with(BASE, BASED_ON_USER__BASE)
            .with(ROLES, ACTIVE_ROLES, INACTIVE_ROLES);

    /// Returns users with roles.
    ///
    List<User> findAllUsersWithRoles();

    /// Returns the users without their roles.
    ///
    List<User> findAllUsers();

    /// Returns a user with the specified id. The returned user is with initialised set of roles.
    ///
    User findUserByIdWithRoles(Long id);

    /// Returns the user with specified key. The returned user also have associated user roles.
    ///
    User findUserByKeyWithRoles(String key);

    /// Resets the user password.
    ///
    UserSecret resetPasswd(final User user, final String passwd);

    /// Tries to find a user by its password reset UUID.
    ///
    Optional<User> findUserByResetUuid(final String uuid);

    /// Generates a temporal password reset UUID for a user that is identified by the provided username or email address.
    /// The generated UUID gets immediately associated with the user, and the updated user is returned for further use, such as email sending with reset URI.
    ///
    /// An empty optional value is returned in case where no user was identified by the given username or email address.
    ///
    /// @param usernameOrEmail either a username or their email address
    /// @param expirationTime  the moment in time at which generated UUID will become expired;
    ///                        it is expressed as a parameter to support different expiration periods for different circumstances, and to facilitate testing;
    ///                        for example, the expiration period for the initially created user could be 24 hours, but 15 minutes for password resets.
    ///
    Optional<UserSecret> assignPasswordResetUuid(final String usernameOrEmail, final Date expirationTime);

    /// Returns `true` if the provided `uuid` is associated with a user and has not yet expired.
    ///
    /// @param uuid a UUID value to validate
    /// @param now  the moment in time at which validation is performed;
    ///             it is expressed as a parameter to facilitate testing.
    ///
    boolean isPasswordResetUuidValid(final String uuid, final Date now);

    /// Estimates password's strength returning `true` if `passwd` is acceptable.
    ///
    boolean isPasswordStrong(final String passwd);

    /// Locks out the account of `username`, which means making a corresponding [User] inactive and removing the user's `password` and `resetUuid`.
    /// The `username` value may belong to a non-existing user, and this function should still perform gracefully.
    ///
    void lockoutUser(final String username);

    /// Returns all available user roles
    ///
    List<? extends UserRole> findAllUserRoles();

    /// Returns the first page of users fetched with user roles.
    /// The page will have less or equal number of items specified by the `capacity` parameter.
    ///
    /// @param capacity a maximum number of users for a data page.
    ///
    IPage<? extends User> firstPageOfUsersWithRoles(int capacity);

    /// Updates association between user and a list of roles.
    /// Once completer user should be associated strictly with the specified list of roles.
    ///
    void updateUsers(Map<User, Set<UserRole>> userRolesMap);

    /// Finds user by name.
    ///
    User findUser(String username);

    /// Returns all active based-on users.
    ///
    Set<User> findBasedOnUsers(final User baseUser, final fetch<User> userFetch);

}
