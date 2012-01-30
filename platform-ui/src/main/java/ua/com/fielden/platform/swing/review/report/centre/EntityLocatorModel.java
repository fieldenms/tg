package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public class EntityLocatorModel<T extends AbstractEntity> extends AbstractEntityCentreModel<T, ILocatorDomainTreeManager> {

    public EntityLocatorModel(final EntityQueryCriteria<ILocatorDomainTreeManager, T, IEntityDao<T>> criteria, final String name) {
	super(criteria, name);
	// TODO Auto-generated constructor stub
    }

}
