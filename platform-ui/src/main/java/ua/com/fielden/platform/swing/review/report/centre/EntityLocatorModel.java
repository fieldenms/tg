package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationModel;

public class EntityLocatorModel<T extends AbstractEntity> extends AbstractEntityCentreModel<T, ILocatorDomainTreeManager> {

    public EntityLocatorModel(final LocatorConfigurationModel<T, ?> configurationModel, final EntityQueryCriteria<ILocatorDomainTreeManager, T, IEntityDao<T>> criteria, final String name) {
	super(configurationModel, criteria, name);
	// TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocatorConfigurationModel<T, ?> getConfigurationModel() {
	return (LocatorConfigurationModel<T, ?>)super.getConfigurationModel();
    }

}
