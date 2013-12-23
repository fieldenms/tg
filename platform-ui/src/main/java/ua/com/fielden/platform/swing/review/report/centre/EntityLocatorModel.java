package ua.com.fielden.platform.swing.review.report.centre;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public class EntityLocatorModel<T extends AbstractEntity<?>> extends AbstractEntityCentreModel<T, ILocatorDomainTreeManagerAndEnhancer> {

    private final List<T> locatorSelectionModel;

    public EntityLocatorModel(//
	    final EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> entityInspectorModel,//
	    final List<T> locatorSelectionModel,//
	    final String name) {
	super(entityInspectorModel, null, name);
	this.locatorSelectionModel = locatorSelectionModel;
    }

    public List<T> getLocatorSelectionModel() {
	return locatorSelectionModel;
    }

    public void resetLocatorSelectionModel() {
	locatorSelectionModel.clear();
    }
}
