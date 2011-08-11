package ua.com.fielden.platform.swing.model;

import java.util.Map;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.swing.review.factory.EntityReviewWithTabsFactory;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.UvEntityCentre;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * Produces a default model and view for entity centres not requiring any specific actions in addition to analysis.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class RfDefault<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityReviewWithTabsFactory<T, DAO, T> {

    private final Class<T> entityType;

    public RfDefault(final Class<T> entityType, final EntityFactory entityFactory, final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController, final String reportName) {
	super(reportName, entityFactory, entityMasterFactory, locatorController);
	this.entityType = entityType;

    }

    @Override
    public CmDefault<T, DAO> createModel(final DynamicEntityQueryCriteria<T, DAO> criteria, final PropertyTableModelBuilder<T> builder, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final int columns, final Map<String, PropertyPersistentObject> criteriaProperties, final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions) {
	return new CmDefault<T, DAO>(entityType, getEntityFactory(), criteria, builder, criteriaProperties, actionChangerBuilder, panelBuilder, columns, getEntityMasterFactory(), getLocatorController(), locatorPersistentObject, afterRunActions);
    }

    @Override
    public DynamicEntityReview<T, DAO, T> createView(final DynamicEntityReviewModel<T, DAO, T> model, final boolean loadRecordByDefault, final DynamicCriteriaModelBuilder<T, DAO, T> modelBuilder, final boolean isPrinciple) {
	return new UvEntityCentre<T, DAO, BaseFrame, CmDefault<T, DAO>>((CmDefault) model, loadRecordByDefault, isPrinciple, modelBuilder, getReportName(), getDetailsCache());
    }

}
