package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.security.user.User;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@EntityType(McpAuthorisationCode.class)
public class McpAuthorisationCodeDao extends CommonEntityDao<McpAuthorisationCode> implements McpAuthorisationCodeCo {

    @Override
    public McpAuthorisationCode new_() {
        return super.new_().setActive(true);
    }

    @Override
    protected IFetchProvider<McpAuthorisationCode> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    public void revokeAllForUser(final User user) {
        final McpAuthorisationCodeCo co$McpAuthorisationCode = co$(McpAuthorisationCode.class);
        try (final var stream = co$McpAuthorisationCode.stream(from(select(McpAuthorisationCode.class)
                                                                    .where().prop(McpAuthorisationCode.USER).eq().val(user)
                                                                            .and().prop(ACTIVE).eq().val(true)
                                                                    .model())
                                                               .with(co$McpAuthorisationCode.getFetchProvider().fetchModel())
                                                               .model())) {
            stream.forEach(code -> co$McpAuthorisationCode.save(code.setActive(false)));
        }
    }

}
