package ua.com.fielden.platform.dao.filtering;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.reflection.Finder.IPropertyPathFilteringCondition;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;

import com.google.inject.Inject;

/**
 * This is a filter providing a mock implementation to be used for testing purposes.
 *
 * @author TG Team
 *
 */
public class DataFilter implements IFilter {

    private final IWorkshopDao daoWorkshop;

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
    protected DataFilter(final IWorkshopDao daoWorkshop) {
	this.daoWorkshop = daoWorkshop;
    }

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
	if (username == null) {
	    return null;
	}

	// TODO clean up properly
	return null;
//	if (entityType == Workshop.class) {
//	    return select(entityType).where().prop("key").eq().val(username).model();
//	}
//
//	if (entityType == EntityAggregates.class || EntityUtils.isQueryBasedEntityType(entityType)) {
//	    throw new UnsupportedOperationException("Unsupported type for filtering enhancement: " + entityType.getName());
//	} else {
//	    final List<String> properties = Finder.findPathsForPropertiesOfType(entityType, Workshop.class, filter);
//	    if (properties.isEmpty()) {
//		return null;
//	    } else {
//		final Workshop wc = daoWorkshop.findByKey(username);
//
//		if (wc == null) {
//		    return null;
//		}
//
//		ICompoundCondition0<ET> cc = select(entityType).where().prop(properties.get(0)).eq().val(wc);
//
//		for (int index = 1; index < properties.size(); index++) {
//		    cc = cc.and().prop(properties.get(index)).eq().val(wc); // all conditions are linked with AND by default
//		}
//
//		return cc.model();
//	    } // else
//	}
    }

}
