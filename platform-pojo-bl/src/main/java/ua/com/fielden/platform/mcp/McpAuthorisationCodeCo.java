package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.mcp.McpAuthorisationCode.*;

public interface McpAuthorisationCodeCo extends IEntityDao<McpAuthorisationCode> {

    IFetchProvider<McpAuthorisationCode> FETCH_PROVIDER = EntityUtils.fetch(McpAuthorisationCode.class)
            .with(CODE, EXPIRATION_DATE, ACTIVE, USER, CLIENT_ID, CODE_CHALLENGE, REDIRECT_URI, RESOURCE);

    /// Revokes all authorisation codes issued to the user.
    ///
    void revokeAllForUser(User user);

}
