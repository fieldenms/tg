package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

public class NoFilter implements IFilter {

    @Override
    public <T extends AbstractEntity> IQueryModel<T> enhance(final Class<T> entityType, final String username) {
	return null;
    }

}
