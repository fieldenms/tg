package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.util.Date;

/// MCP authorisation code, generated when a client accesses the authorisation endpoint.
///
/// This entity type is not registered by the platform.
/// Therefore, applications that enable MCP must register it themselves.
///
@DenyIntrospection
@EntityTitle("MCP Authorisation Code")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(McpAuthorisationCodeCo.class)
public class McpAuthorisationCode extends AbstractPersistentEntity<DynamicEntityKey> {

    public static final String
            CODE = "code",
            EXPIRATION_DATE = "expirationDate",
            USER = "user";

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String code;

    @IsProperty
    @MapTo
    @Required
    @PersistentType(userType = IUtcDateTimeType.class)
    private Date expirationDate;

    @IsProperty
    @MapTo
    @Required
    @Title("The user who granted the authorisation.")
    private User user;

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

}
