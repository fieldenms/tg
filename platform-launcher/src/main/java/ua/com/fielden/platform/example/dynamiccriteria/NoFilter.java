package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class NoFilter implements IFilter {

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> enhance(final Class<T> entityType, final String username) {
	return null;
    }
}
