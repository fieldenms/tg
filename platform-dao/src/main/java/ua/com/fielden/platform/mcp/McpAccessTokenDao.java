package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ua.com.fielden.platform.cypher.Checksum.sha256;

@EntityType(McpAccessToken.class)
public class McpAccessTokenDao extends CommonEntityDao<McpAccessToken> implements McpAccessTokenCo {

    @Override
    public String tokenHash(final String tokenValue) {
        return sha256(tokenValue.getBytes(UTF_8));
    }

    @Override
    public Optional<McpAccessToken> findByTokenValue(final String tokenValue) {
        return Optional.ofNullable(findByKeyAndFetch(FETCH_PROVIDER.fetchModel(), tokenHash(tokenValue)));
    }

    @Override
    public boolean isExpired(final McpAccessToken accessToken) {
        return !serverNow().toDate().before(accessToken.getExpirationDate());
    }

    @Override
    protected IFetchProvider<McpAccessToken> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
