package ua.com.fielden.platform.security.session;

import java.util.Date;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;

/**
 * This entity represents a user session with the system. It is used to persist and validate session authenticators.
 * Also, it can be conveniently used to analyse the current user sessions for expiry, last time of access, was it established from a trusted or untrusted device.
 * <p>
 * User session information is volatile. It gets updated each time a new request comes in, session records may get removed completely.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@CompanionObject(IUserSession.class)
@MapEntityTo
@DenyIntrospection
public class UserSession extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "User", desc = "User with whom the session is associated.")
    @CompositeKeyMember(1)
    @SkipEntityExistsValidation
    private User user;

    @IsProperty
    @MapTo
    @Title(value = "Series ID", desc = "Session series identifier, stored in a form of a hash code.")
    @CompositeKeyMember(2)
    private String seriesId;

    @IsProperty
    @MapTo
    @Title(value = "Expiry Time", desc = "Time when the session should expire.")
    private Date expiryTime;

    @IsProperty
    @MapTo
    @Title(value = "Trusted?", desc = "Indicates whether the session was initiated from a trusted by user device.")
    private boolean trusted;

    @IsProperty
    @MapTo
    @Title(value = "Last Access", desc = "The time when the session was last accessed.")
    private Date lastAccess;

    private Optional<Authenticator> authenticator = Optional.empty();

    public UserSession setAuthenticator(final Authenticator authenticator) {
        this.authenticator = Optional.of(authenticator);
        return this;
    }

    public Optional<Authenticator> getAuthenticator() {
        return authenticator;
    }

    @Observable
    public UserSession setLastAccess(final Date lastAccess) {
        this.lastAccess = lastAccess;
        return this;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    @Observable
    public UserSession setTrusted(final boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    public boolean isTrusted() {
        return trusted;
    }


    @Observable
    public UserSession setExpiryTime(final Date expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    @Observable
    public UserSession setSeriesId(final String seriesId) {
        this.seriesId = seriesId;
        return this;
    }

    public String getSeriesId() {
        return seriesId;
    }

    @Observable
    public UserSession setUser(final User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

}