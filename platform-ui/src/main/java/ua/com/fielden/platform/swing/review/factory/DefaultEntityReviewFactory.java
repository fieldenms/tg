package ua.com.fielden.platform.swing.review.factory;

import java.util.Map;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

import com.google.inject.Inject;

/**
 * Produces plain {@link DynamicEntityReviewModel} suitable for all ad hoc reports with no custom behaviour.
 * 
 * @author 01es
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class DefaultEntityReviewFactory<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> implements IEntityReviewFactory<T, DAO, R> {

    private final IEntityMasterManager entityMasterFactory;
    private final ILocatorConfigurationController locatorController;

    @Inject
    public DefaultEntityReviewFactory(final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController) {
	this.entityMasterFactory = entityMasterFactory;
	this.locatorController = locatorController;
    }

    @Override
    public DynamicEntityReview<T, DAO, R> createView(final DynamicEntityReviewModel<T, DAO, R> model, final boolean loadRecordByDefault, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder, final boolean isPrinciple) {
	return new DynamicEntityReview<T, DAO, R>(model, loadRecordByDefault, isPrinciple, modelBuilder);
    }

    @Override
    public DynamicEntityReviewModel<T, DAO, R> createModel(final DynamicEntityQueryCriteria<T, DAO> criteria, final PropertyTableModelBuilder<T> builder, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final int columns, final Map<String, PropertyPersistentObject> criteriaProperties, final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions) {
	return new DynamicEntityReviewModel<T, DAO, R>(criteria, builder, actionChangerBuilder, locatorController, locatorPersistentObject, columns, criteriaProperties, entityMasterFactory);
    }

}
