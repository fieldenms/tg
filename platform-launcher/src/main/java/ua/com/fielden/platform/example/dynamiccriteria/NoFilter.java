package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

public class NoFilter implements IFilter {

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
        return null;
    }
}
