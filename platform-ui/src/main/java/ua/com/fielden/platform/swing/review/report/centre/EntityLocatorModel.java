package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public class EntityLocatorModel<T extends AbstractEntity<?>> extends AbstractEntityCentreModel<T, ILocatorDomainTreeManagerAndEnhancer> {

    public EntityLocatorModel(final EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> entityInspectorModel, final String name) {
	super(entityInspectorModel, null, name);
	// TODO Auto-generated constructor stub
    }
}
