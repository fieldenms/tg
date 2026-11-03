package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;

/// MCP access token, issued by the token endpoint in exchange for an authorisation code.
///
/// This entity type is not registered by the platform.
/// Therefore, applications that enable MCP must register it themselves.
///
@DenyIntrospection
@EntityTitle("MCP Access Token")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(McpAccessTokenCo.class)
public class McpAccessToken extends AbstractPersistentEntity<DynamicEntityKey> {

    public static final String
            TOKEN_HASH = "tokenHash",
            EXPIRATION_DATE = "expirationDate",
            AUTHORISATION_CODE = "authorisationCode";

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String tokenHash;

    @IsProperty
    @MapTo
    @Required
    @PersistentType(userType = IUtcDateTimeType.class)
    private Date expirationDate;

    @IsProperty
    @MapTo
    @Required
    @Unique
    private McpAuthorisationCode authorisationCode;

    public String getTokenHash() {
        return tokenHash;
    }

    @Observable
    public McpAccessToken setTokenHash(final String tokenHash) {
        this.tokenHash = tokenHash;
        return this;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    @Observable
    public McpAccessToken setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public McpAuthorisationCode getAuthorisationCode() {
        return authorisationCode;
    }

    @Observable
    public McpAccessToken setAuthorisationCode(final McpAuthorisationCode authorisationCode) {
        this.authorisationCode = authorisationCode;
        return this;
    }

}
