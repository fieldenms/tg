package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.ActivePropertyValidator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;

/// MCP authorisation code, generated when a client accesses the authorisation endpoint.
///
/// A code is bound to the authorisation request it was issued for, which the token endpoint checks when the code is redeemed.
/// An inactive code has been redeemed.
///
/// This entity type is not registered by the platform.
/// Therefore, applications that enable OAuth-based authentication of MCP requests must register it themselves.
///
@DenyIntrospection
@EntityTitle("MCP Authorisation Code")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(McpAuthorisationCodeCo.class)
public class McpAuthorisationCode extends ActivatableAbstractEntity<DynamicEntityKey> {

    public static final String
            CODE = "code",
            EXPIRATION_DATE = "expirationDate",
            USER = "user",
            CLIENT_ID = "clientId",
            CODE_CHALLENGE = "codeChallenge",
            REDIRECT_URI = "redirectUri",
            RESOURCE = "resource";

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String code;

    @IsProperty
    @MapTo
    @Required
    @PersistentType(userType = IUtcDateTimeType.class)
    private Date expirationDate;

    /// Codes are not counted as references to the user, so that the user can be deactivated regardless of them.
    /// Declaring [McpAuthorisationCode] among the deactivatable dependencies of [User] is not an option, as it may not be registered.
    ///
    @IsProperty
    @MapTo
    @Required
    @SkipActivatableTracking
    @Title("The user who granted the authorisation.")
    private User user;

    @IsProperty
    @MapTo
    @Required
    private String clientId;

    /// The `S256` code challenge, as sent by the client.
    ///
    @IsProperty
    @MapTo
    @Required
    private String codeChallenge;

    @IsProperty
    @MapTo
    @Required
    private String redirectUri;

    @IsProperty
    @MapTo
    @Required
    private String resource;

    /// Redeclared to exclude it from automatic conflict resolution, so that of concurrent redemptions of the same code, only one succeeds.
    ///
    @IsProperty
    @MapTo(autoConflictResolution = false)
    @Title(value = "Active?", desc = "Designates whether this code is active.")
    @BeforeChange(@Handler(ActivePropertyValidator.class))
    private boolean active;

    public String getResource() {
        return resource;
    }

    @Observable
    public McpAuthorisationCode setResource(final String resource) {
        this.resource = resource;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    @Observable
    public McpAuthorisationCode setRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    @Observable
    public McpAuthorisationCode setCodeChallenge(final String codeChallenge) {
        this.codeChallenge = codeChallenge;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    @Observable
    public McpAuthorisationCode setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Observable
    public McpAuthorisationCode setUser(final User user) {
        this.user = user;
        return this;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    @Observable
    public McpAuthorisationCode setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public String getCode() {
        return code;
    }

    @Observable
    public McpAuthorisationCode setCode(final String code) {
        this.code = code;
        return this;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    @Observable
    public McpAuthorisationCode setActive(final boolean active) {
        this.active = active;
        return this;
    }

}
