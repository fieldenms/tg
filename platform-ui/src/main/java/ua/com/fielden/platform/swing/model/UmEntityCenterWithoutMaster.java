package ua.com.fielden.platform.swing.model;

import java.awt.event.MouseListener;
import java.util.Map;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
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

public abstract class UmEntityCenterWithoutMaster<T extends AbstractEntity, DAO extends IEntityDao<T>, F extends BaseFrame> extends UmEntityCentre<T, DAO, F> {

    private final ActionPanelBuilder panelBuilder;

    public UmEntityCenterWithoutMaster(final IEntityProducer<T> entityProducer,//
    final EntityFactory entityFactory,//
    final DynamicEntityQueryCriteria<T, DAO> criteria,//
    final PropertyTableModelBuilder<T> builder, //
    final Map<String, PropertyPersistentObject> criteriaProperties,//
    final ActionChangerBuilder actionChangerBuilder, //
    final ActionPanelBuilder panelBuilder, //
    final int columns, final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController,//
    final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions) {
	super(entityProducer, entityFactory, criteria, builder, criteriaProperties, actionChangerBuilder, panelBuilder, columns, entityMasterFactory, locatorController, locatorPersistentObject, afterRunActions);
	this.panelBuilder = panelBuilder;
    }

    @Override
    public ActionPanelBuilder getActionPanelBuilder() {
	return panelBuilder;
    }

    public MouseListener getOpenMasterDoubleClickListener() {
	return null;
    }

}
