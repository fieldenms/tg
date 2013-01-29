package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

/**
 * Non-mutable {@link IFilter} implementation, which serves as a default implementation.
 *
 * @author TG Team
 *
 */
public class DefaultFilter implements IFilter {

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
	return null;
    }
}
