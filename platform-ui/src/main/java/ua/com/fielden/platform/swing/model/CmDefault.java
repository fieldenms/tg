package ua.com.fielden.platform.swing.model;

import java.util.Map;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * A generic model for surrogate entity centre view.
 * 
 * @author TG Teams
 * 
 */
public class CmDefault<T extends AbstractEntity, DAO extends IEntityDao<T>> extends UmEntityCenterWithoutMaster<T, DAO, BaseFrame> {

    public CmDefault(//
    final Class<T> entityType, final EntityFactory entityFactory,//
    final DynamicEntityQueryCriteria<T, DAO> criteria,//
    final PropertyTableModelBuilder<T> builder,//
    final Map<String, PropertyPersistentObject> criteriaProperties,//
    final ActionChangerBuilder actionChangerBuilder,//
    final ActionPanelBuilder panelBuilder, //
    final int columns, final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController,//
    final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions) {//
	super(new DefaultEntityProducer<T>(entityFactory, entityType), entityFactory, criteria, builder, criteriaProperties, actionChangerBuilder, panelBuilder, columns, entityMasterFactory, locatorController, locatorPersistentObject, afterRunActions);
    }
    //    @Override
    //    protected BaseFrame createFrame(final T entity) {
    //	throw new UnsupportedOperationException("Editing or manual creation of surrogate entities is not supported.");
    //    }
}
