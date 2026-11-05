package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ua.com.fielden.platform.cypher.Checksum.sha256;

@EntityType(McpAccessToken.class)
public class McpAccessTokenDao extends CommonEntityDao<McpAccessToken> implements McpAccessTokenCo {

    @Override
    public McpAccessToken new_() {
        return super.new_().setActive(true);
    }

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
    @SessionRequired
    public void revoke(final McpAccessToken accessToken) {
        final McpAccessTokenCo co$ = co$(McpAccessToken.class);
        co$.save(co$.findById(accessToken.getId(), FETCH_PROVIDER.fetchModel()).setActive(false));
    }

    @Override
    protected IFetchProvider<McpAccessToken> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
