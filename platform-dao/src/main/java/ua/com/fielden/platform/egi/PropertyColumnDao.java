package ua.com.fielden.platform.egi;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;

public class PropertyColumnDao extends CommonEntityDao<PropertyColumn> implements IPropertyColumn {

    @Inject
    protected PropertyColumnDao(final IFilter filter) {
        super(filter);
    }

}
