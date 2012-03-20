package ua.com.fielden.platform.dao.filtering;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Finder.IPropertyPathFilteringCondition;
import ua.com.fielden.platform.test.domain.entities.Workshop;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao2;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * This is a filter providing a mock implementation to be used for testing purposes.
 *
 * @author TG Team
 *
 */
public class DataFilter2 implements IFilter {

    private final IWorkshopDao2 daoWorkshop;

    private final IPropertyPathFilteringCondition filter = new IPropertyPathFilteringCondition() {
	@Override
	public boolean ignore(final String propertyName) {
	    return false;
	}

	@Override
	public boolean ignore(final Class<?> enttyType) {
	    return false;
	}
    };

    @Inject
    protected DataFilter2(final IWorkshopDao2 daoWorkshop) {
	this.daoWorkshop = daoWorkshop;
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> enhance(final Class<T> entityType, final String workshop) {
	if (workshop == null) {
	    return null;
	}

	if (entityType == Workshop.class) {
	    return select(entityType).where().prop("key").eq().val(workshop).model();
	}

	if (entityType == EntityAggregates.class || IQueryModelProvider.class.isAssignableFrom(entityType)) {
	    throw new UnsupportedOperationException("Unsupported type for filtering enhancement: " + entityType.getName());
	} else {
	    final List<String> properties = Finder.findPathsForPropertiesOfType(entityType, Workshop.class, filter);
	    if (properties.isEmpty()) {
		return null;
	    } else {
		final Workshop wc = daoWorkshop.findByKey(workshop);

		if (wc == null) {
		    return null;
		}

		ICompoundCondition0 cc = select(entityType).where().prop(properties.get(0)).eq().val(wc);

		for (int index = 1; index < properties.size(); index++) {
		    cc = cc.and().prop(properties.get(index)).eq().val(wc); // all conditions are linked with AND by default
		}

		return cc.model();
	    } // else
	}
    }

}
