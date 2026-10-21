package ua.com.fielden.platform.security.user;

import org.joda.time.DateTime;
import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;

import java.util.Date;
import java.util.Optional;

public interface UserSecretCo extends IEntityDao<UserSecret>, ISaveWithFetch<UserSecret> {

    int RESET_UUID_EXPIRATION_IN_MUNUTES = 15;
    int INITIAL_USER_UUID_EXPIRATION_IN_MUNUTES = 60 * 24;

    static Date passwordResetExpirationTime() {
        // Server-side timezone should always be used for calculating the expiration time.
        // Hence, new DateTime().
        return new DateTime().plusMinutes(RESET_UUID_EXPIRATION_IN_MUNUTES).toDate();
    }

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

    static Date newUserPasswordRestExpirationTime() {
        // Server-side timezone should always be used for calculating the expiration time.
        // Hence, new DateTime().
        return new DateTime().plusMinutes(INITIAL_USER_UUID_EXPIRATION_IN_MUNUTES).toDate();
    }

    /**
     * A convenient method to find user's secret by their name.
     *
     * @param username
     * @return
     */
    Optional<UserSecret> findByUsername(final String username);

    /**
     * A method for hashing the user password before storing it into the database.
     * 
     * @param passwd
     * @param salt
     * @return
     */
    default String hashPasswd(final String passwd, final String salt) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * A convenient method for generation of new salt for cryptographic hashing.
     *
     * @return
     */
    String newSalt();

}
