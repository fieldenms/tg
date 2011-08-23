package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * Non-mutable {@link IFilter} implementation, which serves as a default implementation.
 *
 * @author TG Team
 *
 */
public class DefaultFilter implements IFilter {

     @Override
    public <T extends AbstractEntity> EntityResultQueryModel<T> enhance(final Class<T> entityType, final String username) {
	return null;
    }
}
