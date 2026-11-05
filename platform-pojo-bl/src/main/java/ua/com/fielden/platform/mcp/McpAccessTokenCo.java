package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Optional;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;

public interface McpAccessTokenCo extends IEntityDao<McpAccessToken> {

    IFetchProvider<McpAccessToken> FETCH_PROVIDER = EntityUtils.fetch(McpAccessToken.class)
            .with(McpAccessToken.TOKEN_HASH, McpAccessToken.EXPIRATION_DATE, McpAccessToken.RESOURCE, ACTIVE)
            .with(McpAccessToken.AUTHORISATION_CODE + "." + McpAuthorisationCode.USER,
                  McpAccessToken.AUTHORISATION_CODE + "." + McpAuthorisationCode.CLIENT_ID,
                  McpAccessToken.AUTHORISATION_CODE + "." + McpAuthorisationCode.EXPIRATION_DATE,
                  McpAccessToken.AUTHORISATION_CODE + "." + McpAuthorisationCode.RESOURCE);

    /// Returns the hash of an access token value, which is what [McpAccessToken#getTokenHash()] holds.
    ///
    String tokenHash(String tokenValue);

    /// Finds an access token by its value, as issued to a client.
    ///
    Optional<McpAccessToken> findByTokenValue(String tokenValue);

    /// Determines whether `accessToken` has expired, based on the current server time.
    ///
    boolean isExpired(McpAccessToken accessToken);

    /// Revokes `accessToken`, so that it can no longer be used.
    ///
    void revoke(McpAccessToken accessToken);

}
