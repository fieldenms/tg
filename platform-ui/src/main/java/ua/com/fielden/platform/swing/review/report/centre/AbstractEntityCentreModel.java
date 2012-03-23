package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.ei.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AbstractCentreConfigurationModel;

public class AbstractEntityCentreModel<T extends AbstractEntity, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractEntityReviewModel<T, CDTME> {

    private final String name;
    private final EntityInspectorModel<EntityQueryCriteria<CDTME, T, IEntityDao<T>>> entityInspectorModel;
    private final IEntityMasterManager masterManager;


    public AbstractEntityCentreModel(final AbstractCentreConfigurationModel<T, CDTME> configurationModel, final EntityInspectorModel<EntityQueryCriteria<CDTME, T, IEntityDao<T>>> entityInspectorModel, final IEntityMasterManager masterManager, final String name) {
	super(configurationModel, entityInspectorModel.getEntity());
	this.entityInspectorModel = entityInspectorModel;
	this.masterManager = masterManager;
	this.name = name;

	// TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractCentreConfigurationModel<T, CDTME> getConfigurationModel() {
	return (AbstractCentreConfigurationModel<T, CDTME>)super.getConfigurationModel();
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
    public EntityInspectorModel<EntityQueryCriteria<CDTME, T, IEntityDao<T>>> getEntityInspectorModel() {
	return entityInspectorModel;
    }

    /**
     * Returns the {@link IEntityMasterManager} for this centre model.
     * 
     * @return
     */
    public IEntityMasterManager getMasterManager() {
	return masterManager;
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
