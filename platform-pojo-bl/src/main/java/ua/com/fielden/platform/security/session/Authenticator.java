package ua.com.fielden.platform.security.session;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.util.Date;
import java.util.Optional;

import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

/// Represents a user session authenticator.
///
public final class Authenticator {

    public static final String AUTHENTICATOR_SEPARATOR = "::";

    public final String username;
    public final String seriesId;
    public final Optional<Date> expiryTime;
    public final long version;
    public final String hash;

    public final String token;
    private final String value;

    public Authenticator(final String token, final String hash) {
        this(Optional.empty(), token, hash);
    }

    public Authenticator(final Optional<Date> expiryTime, final String token, final String hash) {
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
        this.value = token + AUTHENTICATOR_SEPARATOR + hash;
    }

    public static String mkToken(final String username, final String seriesId, final long version) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(seriesId) || version < 0) {
            throw new InvalidArgumentException("Token argumens are invalid.");
        }
        return username + AUTHENTICATOR_SEPARATOR + seriesId + AUTHENTICATOR_SEPARATOR + version;
    }

    /// Reconstructs an authenticator from its string representation.
    ///
    public static Authenticator fromString(final String authenticator) {
        if (StringUtils.isEmpty(authenticator)) {
            throw new InvalidArgumentException("Cannot construct an authenticator from an empty string.");
        }

        final String[] parts = authenticator.split(AUTHENTICATOR_SEPARATOR);
        if (parts.length != 4) {
            throw new InvalidArgumentException("The provided string does not represent a valid authenticator.");
        }

        final long version;
        try {
            version = Long.parseLong(parts[2]);
        } catch (final NumberFormatException _) {
            throw new InvalidArgumentException("The provided string does not represent a valid authenticator");
        }

        return new Authenticator(mkToken(parts[0], parts[1], version), parts[3]);
    }

    /// The authenticator's expiry time.
    ///
    public Optional<DateTime> getExpiryTime() {
        return expiryTime.map(DateTime::new);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof Authenticator that && equalsEx(this.value, that.value);

    }

    @Override
    public String toString() {
        return value;
    }

}
