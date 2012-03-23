package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationModel;

public class EntityLocatorModel<T extends AbstractEntity> extends AbstractEntityCentreModel<T, ILocatorDomainTreeManagerAndEnhancer> {

    public EntityLocatorModel(final LocatorConfigurationModel<T, ?> configurationModel, final EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao2<T>>> entityInspectorModel, final String name) {
	super(configurationModel, entityInspectorModel, null, name);
	// TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocatorConfigurationModel<T, ?> getConfigurationModel() {
	return (LocatorConfigurationModel<T, ?>)super.getConfigurationModel();
    }

}
