package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.ei.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AbstractCentreConfigurationModel;

public class AbstractEntityCentreModel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractEntityReviewModel<T, DTM> {

    private final String name;
    private final EntityInspectorModel<EntityQueryCriteria<DTM, T, IEntityDao<T>>> entityInspectorModel;

    public AbstractEntityCentreModel(final AbstractCentreConfigurationModel<T, DTM> configurationModel, final EntityInspectorModel<EntityQueryCriteria<DTM, T, IEntityDao<T>>> entityInspectorModel, final String name) {
	super(configurationModel, entityInspectorModel.getEntity());
	this.entityInspectorModel = entityInspectorModel;
	this.name = name;
	// TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractCentreConfigurationModel<T, DTM> getConfigurationModel() {
	return (AbstractCentreConfigurationModel<T, DTM>)super.getConfigurationModel();
    }

    /**
     * Returns the name of the entity centre. If the name is null then entity centre is principle, otherwise it is non principle entity centre.
     * 
     * @return
     */
    public String getName() {
	return name;
    }

    /**
     * Returns the {@link EntityInspectorModel} for this entity centre.
     * 
     * @return
     */
    public EntityInspectorModel<EntityQueryCriteria<DTM, T, IEntityDao<T>>> getEntityInspectorModel() {
	return entityInspectorModel;
    }

    /**
     * Determines whether this entity centre's model is valid or not.
     * 
     * @return
     */
    public Result validate() {
	// TODO Auto-generated method stub
	return null;
    }

}
