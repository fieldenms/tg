package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.mcp.validators.McpAccessTokenActiveUserValidator;
import ua.com.fielden.platform.mcp.validators.McpAccessTokenAuthorisationCodeValidator;
import ua.com.fielden.platform.mcp.validators.McpAccessTokenResourceValidator;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;

/// MCP access token, issued by the token endpoint in exchange for an authorisation code.
///
/// This entity type is not registered by the platform.
/// Therefore, applications that enable OAuth-based authentication of MCP requests must register it themselves.
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
            AUTHORISATION_CODE = "authorisationCode",
            RESOURCE = "resource";

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String tokenHash;

    @IsProperty
    @MapTo
    @Required
    @PersistentType(userType = IUtcDateTimeType.class)
    private Date expirationDate;

    /// The code that was redeemed for this token, and therefore is inactive.
    ///
    @IsProperty
    @MapTo
    @Required
    @Unique
    @SkipEntityExistsValidation(skipActiveOnly = true)
    @Dependent(RESOURCE)
    @BeforeChange({@Handler(McpAccessTokenAuthorisationCodeValidator.class),
                   @Handler(McpAccessTokenActiveUserValidator.class)})
    private McpAuthorisationCode authorisationCode;

    /// Resource identifier of the MCP server that this token was issued for.
    ///
    @IsProperty
    @MapTo
    @Required
    @BeforeChange(@Handler(McpAccessTokenResourceValidator.class))
    private String resource;

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

    public String getResource() {
        return resource;
    }

    @Observable
    public McpAccessToken setResource(final String resource) {
        this.resource = resource;
        return this;
    }

}
