package ua.com.fielden.platform.security.session;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This is a convenient abstraction for representing a user session authenticator.
 *
 * @author TG Team
 *
 */
public final class Authenticator {
    public static final String AUTHENTICATOR_SEPARATOR = "::";

    public final String username;
    public final String seriesId;
    public final Optional<Date> expiryTime;
    public final long version;
    public final String hash;

    public final String token;
    private final String value;

    public Authenticator(
            final String token,
            final String hash) {
        this(Optional.empty(), token, hash);
    }

    public Authenticator(
            final Optional<Date> expiryTime,
            final String token,
            final String hash) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(hash)) {
            throw new IllegalArgumentException("Authenticator argumens are invalid.");
        }

        final String[] tokenParts = token.split(AUTHENTICATOR_SEPARATOR);
        if (tokenParts.length != 3) {
            throw new IllegalArgumentException("Invalid token structure.");
        }

        this.username = tokenParts[0];
        this.seriesId = tokenParts[1];
        this.version = Long.parseLong(tokenParts[2]);
        this.hash = hash;
        this.expiryTime = expiryTime;

        this.token = token;

        this.value = new StringBuilder()
            .append(token).append(AUTHENTICATOR_SEPARATOR)
            .append(hash).toString();
 
    }

    
    /**
     * Constructs a token from the provided parts.
     *
     * @param username
     * @param seriesId
     * @param expiryTime
     * @return
     */
    public static String mkToken(
            final String username,
            final String seriesId,
            final long version) {
        if (StringUtils.isEmpty(username) ||
            StringUtils.isEmpty(seriesId) ||
            version < 0) {
                throw new IllegalArgumentException("Token argumens are invalid.");
            }

        return new StringBuilder()
            .append(username).append(AUTHENTICATOR_SEPARATOR)
            .append(seriesId).append(AUTHENTICATOR_SEPARATOR)
            .append(version).toString();

    }

    /**
     * Reconstructs an authenticator from its string representation.
     *
     * @param authenticator
     * @return
     */
    public static Authenticator fromString(final String authenticator) {
        if (StringUtils.isEmpty(authenticator)) {
            throw new IllegalArgumentException("Cannot construct an authenticator from an empty string.");
        }

        final String[] parts = authenticator.split(AUTHENTICATOR_SEPARATOR);
        if (parts.length != 4) {
            throw new IllegalArgumentException("The provided string does not represent a valid authenticator.");
        }

        final long version;
        try {
            version = Long.parseLong(parts[2]);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("The provided string does not represent a valid authenticator");
        }

        final String token = mkToken(parts[0], parts[1], version);

        return new Authenticator(token, parts[3]);
    }

    /**
     * A convenient method for obtaining authenticator's expiry time as an instance of date rather than long.
     *
     * @return
     */
    public Optional<DateTime> getExpiryTime() {
        return expiryTime.map(DateTime::new);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Authenticator)) {
            return false;
        }

        final Authenticator that = (Authenticator) obj;
        return EntityUtils.equalsEx(this.value, that.value);
    }

    @Override
    public String toString() {
        return value;
    }

}
