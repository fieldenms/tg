package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

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

}
