package ua.com.fielden.platform.entity.query;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

/**
 * This implementation serves as a convenient stub, which does not filter any data.
 *
 * @author TG Team
 */
@Singleton
public class NoDataFilter implements IFilter {

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
        return null;
    }
}
