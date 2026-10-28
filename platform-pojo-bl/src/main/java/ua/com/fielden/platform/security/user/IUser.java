/**
 *
 */
package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.security.user.User.*;

/// Contract for DAO handling user instances.
///
public interface IUser extends IEntityDao<User>, ISaveWithFetch<User> {

    IFetchProvider<User> FETCH_PROVIDER = EntityUtils.fetch(User.class)
            .with(KEY, EMAIL, ACTIVE, SSO_ONLY)
            .with(BASE, BASED_ON_USER__BASE)
            .with(ACTIVE_ROLES, INACTIVE_ROLES);

    fetch<User> FETCH_MODEL = FETCH_PROVIDER.fetchModel();

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

    /// Generates a temporary password reset UUID for a user that is identified by the provided username or email address.
    /// The generated UUID gets immediately associated with the user, and the updated user is returned for further use, such as email sending with reset URI.
    ///
    /// An empty optional is returned if no user was identified.
    ///
    /// Equivalent to [#findUserForPasswordReset(String)] followed by [#assignPasswordResetUuid(User, Date)].
    ///
    /// @param usernameOrEmail either a username or their email address
    /// @param expirationTime  the moment in time at which generated UUID will become expired;
    ///                        it is expressed as a parameter to support different expiration periods for different circumstances, and to facilitate testing;
    ///                        for example, the expiration period for the initially created user could be 24 hours, but 15 minutes for password resets.
    ///
    Optional<UserSecret> assignPasswordResetUuid(final String usernameOrEmail, final Date expirationTime);

    /// Finds the user identified by the provided username or email address (case-insensitive) who can reset their password.
    /// Such a user is active and, in the SSO authentication mode, is not restricted to SSO only.
    ///
    /// An empty optional is returned if no such user was identified.
    ///
    Optional<User> findUserForPasswordReset(final String usernameOrEmail);

    /// Generates a temporary password reset UUID for `user`, associates it with the user, and returns the updated secret.
    /// A previously generated UUID of `user` is replaced, and thus can no longer be used.
    ///
    /// @param user           a user, such as one found by [#findUserForPasswordReset(String)]
    /// @param expirationTime the moment in time at which generated UUID will become expired
    ///
    UserSecret assignPasswordResetUuid(final User user, final Date expirationTime);

    /// Returns the secret of `user` with a password reset UUID that remains valid at least until `validUntil`.
    ///
    /// The UUID already assigned to `user` is kept if it expires after `validUntil`, so that a link sent with it earlier remains usable,
    /// and if it does not expire after `expirationTime`, so that a UUID with a longer validity, such as that of the activation link for a new user, is replaced rather than sent again.
    /// Otherwise, a new UUID that expires at `expirationTime` is assigned, as by [#assignPasswordResetUuid(User, Date)].
    /// In other words, the UUID is kept if its expiration time is in `(validUntil, expirationTime]`.
    ///
    /// @param user           a user, such as one found by [#findUserForPasswordReset(String)]
    /// @param validUntil     the moment in time until which an already assigned UUID needs to remain valid to be kept
    /// @param expirationTime the moment in time at which a newly generated UUID will become expired, and after which an already assigned UUID must not expire to be kept
    ///
    UserSecret findOrAssignPasswordResetUuid(final User user, final Date validUntil, final Date expirationTime);

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
    /// Only an account that can sign in with a password, as determined by [#canSignInWithPassword(String)], is locked out.
    /// Failed password attempts pose no risk to other accounts, such as accounts restricted to SSO in the SSO authentication mode, and do not deactivate them.
    /// The `username` value may belong to a non-existing user, and this function should still perform gracefully.
    ///
    /// Deactivation saves the user, which requires a current user.
    /// A caller acting on behalf of an unauthenticated request, such as a login attempt, sets [User.system_users#SU] as the current user.
    /// The account's secret is removed even if its deactivation fails, so that the account can no longer sign in with a password.
    ///
    void lockoutUser(final String username);

    /// Determines whether the user identified by the provided username (case-insensitive) exists and can sign in with a password.
    /// Such a user is active and, in the SSO authentication mode, is not restricted to SSO only.
    ///
    /// The user is not retrieved, as this method is invoked for every login attempt.
    ///
    boolean canSignInWithPassword(final String username);

    /// Returns all available user roles
    ///
    List<? extends UserRole> findAllUserRoles();

    /// Returns the first page of users fetched with user roles.
    /// The page will have less or equal number of items specified by the `capacity` parameter.
    ///
    /// @param capacity a maximum number of users for a data page.
    ///
    IPage<? extends User> firstPageOfUsersWithRoles(int capacity);

    /// Finds user by name.
    ///
    User findUser(String username);

    /// Returns all active based-on users.
    ///
    Set<User> findBasedOnUsers(final User baseUser, final fetch<User> userFetch);

}
