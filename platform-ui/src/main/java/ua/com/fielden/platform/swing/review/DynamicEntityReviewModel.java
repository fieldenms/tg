package ua.com.fielden.platform.swing.review;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.ei.CriteriaInspectorModel;
import ua.com.fielden.platform.swing.ei.DynamicReviewInspectorModel;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * Model for {@link DynamicEntityReview}.
 * 
 * @author TG Team
 * 
 */
public class DynamicEntityReviewModel<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends EntityReviewModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> {

    private final int columns;

    private final ActionChangerBuilder actionChangerBuilder;

    private final Map<String, PropertyPersistentObject> criteriaProperties;

    public DynamicEntityReviewModel(//
    final DynamicEntityQueryCriteria<T, DAO> criteria,//
    final PropertyTableModelBuilder<T> builder, //
    final ActionChangerBuilder actionChangerBuilder,//
    final ILocatorConfigurationController locatorController,//
    final LocatorPersistentObject locatorPersistentObject,//
    final int columns, //
    final Map<String, PropertyPersistentObject> criteriaProperties,//
    final IEntityMasterManager entityMasterFactory,//
    final Runnable... afterRunActions) {
	this(criteria, builder, new DynamicReviewPropertyBinder<T, DAO>(entityMasterFactory, locatorController, locatorPersistentObject), actionChangerBuilder, columns, criteriaProperties, entityMasterFactory, afterRunActions);
    }

    protected DynamicEntityReviewModel(//
    final DynamicEntityQueryCriteria<T, DAO> criteria,//
    final PropertyTableModelBuilder<T> builder,//
    final DynamicReviewPropertyBinder<T, DAO> propertyBinder,//
    final ActionChangerBuilder actionChangerBuilder,//
    final int columns, //
    final Map<String, PropertyPersistentObject> criteriaProperties,//
    final IEntityMasterManager entityMasterFactory,//
    final Runnable... afterRunActions) {
	super(criteria, builder, propertyBinder, entityMasterFactory, afterRunActions);
	this.actionChangerBuilder = actionChangerBuilder;
	this.columns = columns;
	this.criteriaProperties = criteriaProperties;
    }

    @Override
    public DynamicReviewPropertyBinder<T, DAO> getPropertyBinder() {
	return (DynamicReviewPropertyBinder<T, DAO>) super.getPropertyBinder();
    }

    @Override
    protected CriteriaInspectorModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> createInspectorModel(final DynamicEntityQueryCriteria<T, DAO> criteria) {
	return new DynamicReviewInspectorModel<T, DAO>(getCriteria(), getPropertyBinder());
    }

    @Override
    public DynamicEntityQueryCriteria<T, DAO> getCriteria() {
	return super.getCriteria();
    }

    public final Map<String, PropertyPersistentObject> getCriteriaProperties() {
	return criteriaProperties;
    }

    public int getColumns() {
	return columns;
    }

    @Override
    protected void enableButtons(final boolean enable) {
	super.enableButtons(enable);
	getEntityReview().enableButtons(enable);
    }

    public void setOrdering(final IOrderSetter orderSetter) {
	orderSetter.setOrder(this);
    }

    @Override
    public DynamicEntityReview<T, DAO, R> getEntityReview() {
	return (DynamicEntityReview<T, DAO, R>) super.getEntityReview();
    }

    public ActionChangerBuilder getActionChangerBuilder() {
	return actionChangerBuilder;
    }

    @Override
    protected void updateState() {
	super.updateState();

	if (getEntityReview() instanceof DynamicEntityReviewWithTabs) {
	    final DynamicEntityReviewWithTabs<T, DAO, R> derWithTabs = ((DynamicEntityReviewWithTabs<T, DAO, R>) getEntityReview());
	    if (!DynamicEntityReviewWithTabs.gridTabName.equals(derWithTabs.getSelectedTabTitle())) { // assume that lifecycle/aggregation analysis tab is selected:
		// in this case "export" button should be disabled:
		getExport().setEnabled(false, false);
	    }
	}
    }

}
