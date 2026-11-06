package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;

import java.util.Date;
import java.util.Optional;

/// A companion for [UserSecret], which holds the authentication secrets of a user: the hash of their password with its salt, and a password reset UUID.
///
/// A password reset UUID has the form `<username>-<random identifier>-<expiration time>`, with the expiration time in epoch milliseconds by the server clock.
/// Usernames cannot contain the separator, [UserSecret#SECRET_RESET_UUID_SEPERATOR] (see [User#USER_NAME_REGEX]), so that the parts of a reset UUID can be recovered.
/// The same reset UUIDs serve as password reset links and as activation links of new users, which differ in how long they remain valid.
///
public interface UserSecretCo extends IEntityDao<UserSecret>, ISaveWithFetch<UserSecret> {

    /// The number of minutes for which a password reset link remains valid.
    ///
    int RESET_UUID_EXPIRATION_IN_MINUTES = 15;

    /// The number of minutes for which the activation link of a new user remains valid.
    ///
    int INITIAL_USER_UUID_EXPIRATION_IN_MINUTES = 60 * 24;

    /// Returns the expiration time encoded in `resetUuid`, or an empty value if `resetUuid` is empty or malformed.
    ///
    static Optional<Date> resetUuidExpirationTime(final String resetUuid) {
        if (resetUuid == null || resetUuid.isEmpty()) {
            return Optional.empty();
        }
        final String[] uuidParts = resetUuid.split(UserSecret.SECRET_RESET_UUID_SEPERATOR);
        if (uuidParts.length != 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Date(Long.parseLong(uuidParts[2])));
        } catch (final NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /// Returns the secret of the user with `username`, or an empty value if that user has no secret.
    ///
    Optional<UserSecret> findByUsername(String username);

    /// Returns the hash of `passwd` with `salt`, which is stored as the password of a user, and compared with on authentication.
    ///
    /// The DAO implementation hashes with PBKDF2 (HMAC-SHA256); the default implementation throws [UnsupportedOperationException].
    ///
    default String hashPasswd(final String passwd, final String salt) {
        throw new UnsupportedOperationException();
    }
    
    /// Returns a new random salt for hashing a password.
    ///
    String newSalt();

}
