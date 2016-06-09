package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

/**
 * {@link IFilter} contract implementation is required for instantiation of {@link TgTestApplicationServerModule} for Web UI Testing Server.
 * <p>
 * This implementation serves as a convenient stub, which does not filter any data.
 *
 * @author TG Team
 *
 */
public class NoDataFilter implements IFilter {

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
        return null;
    }

}
