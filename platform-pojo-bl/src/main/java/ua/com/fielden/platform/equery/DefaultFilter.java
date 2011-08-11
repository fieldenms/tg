package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

/**
 * Non-mutable {@link IFilter} implementation, which serves as a default implementation.
 *
 * @author TG Team
 *
 */
public class DefaultFilter implements IFilter {

    @Override
    public <T extends AbstractEntity> IQueryModel<T> enhance(final Class<T> entityType, final String username) {
	return null;
    }

}
