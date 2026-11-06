package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.security.user.User;

import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ua.com.fielden.platform.cypher.Checksum.sha256;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

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
    public Optional<McpAccessToken> findByAuthorisationCode(final McpAuthorisationCode authorisationCode) {
        final var query = select(McpAccessToken.class).where().prop(McpAccessToken.AUTHORISATION_CODE).eq().val(authorisationCode).model();
        return getEntityOptional(from(query).with(FETCH_PROVIDER.fetchModel()).model());
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
    public void revokeAllForUser(final User user) {
        final McpAccessTokenCo co$ = co$(McpAccessToken.class);
        try (final var stream = co$.stream(from(select(McpAccessToken.class)
                                                .where().prop(McpAccessToken.AUTHORISATION_CODE + "." + McpAuthorisationCode.USER).eq().val(user)
                                                        .and().prop(ACTIVE).eq().val(true)
                                                .model())
                                           .with(co$.getFetchProvider().fetchModel())
                                           .model())) {
            stream.forEach(token -> co$.save(token.setActive(false)));
        }
    }

    @Override
    protected IFetchProvider<McpAccessToken> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
