package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizardModel;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;

/**
 * {@link EntityReview} which provides dynamic addition of criteria (arranged in several columns) and properties displayed on EGI.
 *
 * @author yura
 *
 */
public class DynamicEntityReview<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends EntityReview<T, DAO, DynamicEntityQueryCriteria<T, DAO>> {

    private static final long serialVersionUID = -8915215849932784789L;

    private boolean shouldCheckModelBuilderBeforeClose = true;
    private final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder;
    /**
     * Indicates whether this {@link DynamicEntityReview} is principle or it is a copy of the associated principle report.
     */
    private final boolean isPrinciple;
    private CriteriaDndPanel<T, DAO> criteriaPanel;
    private JComponent actionChangeButton;
    /**
     * Indicates whether closing was cancelled by user or not.
     */
    private boolean wasClosingCanceled = false;

    public DynamicEntityReview(//
    final DynamicEntityReviewModel<T, DAO, R> model,//
    final boolean showRecords,//
    final boolean isPrinciple, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	super(model, showRecords);
	getEntityGridInspector().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	this.isPrinciple = isPrinciple;
	this.modelBuilder = modelBuilder;

	OpenMasterClickAction.enhanceWithClickAction(getEntityGridInspector().getActualModel().getPropertyColumnMappings(),//
	model.getEntityType(), //
	model.getEntityMasterFactory(), //
	this);
    }

    /**
     * Returns {@link #isPrinciple} value.
     *
     * @return
     */
    public boolean isPrinciple() {
	return isPrinciple;
    }

    protected DynamicCriteriaModelBuilder<T, DAO, R> getModelBuilder() {
	return modelBuilder;
    }

    /**
     * Creates criteria panel.
     *
     * @return
     */
    private CriteriaDndPanel<T, DAO> createAdjustedCriteriaPanel() {
	final Map<String, PropertyPersistentObject> persistentCriteriaProperties = getEntityReviewModel().getCriteriaProperties();
	if (persistentCriteriaProperties.size() == 0) {
	    return null;
	}
	return new CriteriaDndPanel<T, DAO>(persistentCriteriaProperties, getEntityReviewModel().getCriteria(), getEntityReviewModel().getColumns(), getEntityReviewModel().getCriteriaInspectorModel().getEditors());
    }

    @Override
    protected JPanel createCriteriaPanel(final EntityReviewModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> model) {
	criteriaPanel = createAdjustedCriteriaPanel();
	if (criteriaPanel == null) {
	    return null;
	}
	final TaskPanel taskPanel = new TaskPanel(new MigLayout("fill, insets 0"));
	taskPanel.add(criteriaPanel, "grow, wrap");
	taskPanel.setTitle("Selection criteria");
	taskPanel.setAnimated(false);
	return taskPanel;
    }

    @Override
    public String getInfo() {
	return "default info";
    }

    protected CriteriaDndPanel<T, DAO> getCriteriaDndPanel() {
	return criteriaPanel;
    }

    @Override
    public DynamicEntityReviewModel<T, DAO, R> getEntityReviewModel() {
	return (DynamicEntityReviewModel<T, DAO, R>) super.getEntityReviewModel();
    }

    /**
     * Saves information about editor positions, values and ordering to the specified wizardModel.
     *
     * @param wizardModel
     */
    public void saveValues() {
	getEntityReviewModel().commitComponents();
	final AbstractWizardModel<T, DAO, R> wizardModel = modelBuilder.getWizardModel();
	final DynamicEntityQueryCriteria<T, DAO> criteria = getEntityReviewModel().getCriteria();
	for (final PropertyPersistentObject persistentObject : wizardModel.getPersistedCriteria().values()) {
	    final String propertyName = persistentObject.getPropertyName();
	    persistentObject.setPropertyValue(criteria.get(propertyName));
	    final DynamicProperty dynamicProperty = criteria.getEditableProperty(propertyName);
	    persistentObject.setNot(dynamicProperty.getNot());
	    persistentObject.setExclusive(dynamicProperty.getExclusive());
	    persistentObject.setDatePrefix(dynamicProperty.getDatePrefix());
	    persistentObject.setDateMnemonic(dynamicProperty.getDateMnemonic());
	    persistentObject.setAndBefore(dynamicProperty.getAndBefore());
	    persistentObject.setAll(dynamicProperty.getAll());
	    persistentObject.setOrNull(dynamicProperty.getOrNull());
	    persistentObject.setPosition(criteriaPanel.getPositionOf(propertyName));
	}
	wizardModel.setOrderedMappingsPObj(getColumnMappingsPersistentObject());
	if (getEntityReviewModel().getPropertyBinder() != null) {
	    wizardModel.setLocatorPersistentObject(getEntityReviewModel().getPropertyBinder().getLocatorPersistentObject());
	}
    }

    /**
     * Saves the {@link AbstractPropertyColumnMapping}s, their order and sort orders of this entity review.
     *
     * @return
     */
    public PropertyColumnMappingsPersistentObject getColumnMappingsPersistentObject() {
	return new PropertyColumnMappingsPersistentObject(new ArrayList<AbstractPropertyColumnMapping>(getEntityGridInspector().getCurrentColumnsState()), getCurrentSortKeyState(), getCurrentSortableColumns());
    }

    @Override
    protected JPanel createButtonPanel(final EntityReviewModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> model) {
	final DynamicEntityReviewModel<T, DAO, R> dynamicReviewModel = (DynamicEntityReviewModel<T, DAO, R>) model;

	setActionChangeButton(buildActionChanger());

	final String additionalCell = getActionChangeButton() != null ? "[160::,fill]" : "";
	final String designButtonCell = criteriaPanel != null ? "[70::,fill]" : "";
	final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[70::,fill]" + designButtonCell + additionalCell + "20:push[][][][]20[]push[:70:,fill][:70:,fill]", "[c,fill]"));

	buttonPanel.add(getDefaultButton());
	if (criteriaPanel != null) {
	    buttonPanel.add(new JToggleButton(criteriaPanel.getToggleAction()));
	}
	if (getActionChangeButton() != null) {
	    buttonPanel.add(getActionChangeButton());
	}

	buttonPanel.add(navButton(model.getPaginator().getFirst()));
	buttonPanel.add(navButton(model.getPaginator().getPrev()));
	buttonPanel.add(navButton(model.getPaginator().getNext()));
	buttonPanel.add(navButton(model.getPaginator().getLast()));
	buttonPanel.add(getJlFeedback());

	buttonPanel.add(getExportButton());
	buttonPanel.add(getRunButton());

	return buttonPanel;
    }

    protected JComponent buildActionChanger() {
	final List<String> actionOrder = new ArrayList<String>();
	actionOrder.add("Configure");
	actionOrder.add("Save");
	actionOrder.add("Save As");
	if (!isPrinciple()) {
	    actionOrder.add("Delete");
	}
	return getEntityReviewModel().getActionChangerBuilder().buildActionChanger(actionOrder);
    }

    protected JComponent getActionChangeButton() {
	return actionChangeButton;
    }

    protected void setActionChangeButton(final JComponent actionChangeButton) {
	this.actionChangeButton = actionChangeButton;
    }

    public void enableButtons(final boolean enable) {
	if (criteriaPanel != null) {
	    criteriaPanel.getToggleAction().setEnabled(enable);
	}
	if (actionChangeButton != null) {
	    actionChangeButton.setEnabled(enable);
	}
    }

    public boolean shouldCheckModelBuilderBeforeClose() {
	return shouldCheckModelBuilderBeforeClose;
    }

    public void setShouldCheckModelBuilderBeforeClose(final boolean shouldCheckModelBuilderBeforeClose) {
	this.shouldCheckModelBuilderBeforeClose = shouldCheckModelBuilderBeforeClose;
    }

    @Override
    public ICloseGuard canClose() {
	final ICloseGuard result = super.canClose();
	if (result != null) {
	    return result;
	}
	if (shouldCheckModelBuilderBeforeClose()) {
	    final CloseReportOptions chosenOption = modelBuilder.canClose("");
	    wasClosingCanceled = CloseReportOptions.CANCEL == chosenOption;
	    return chosenOption == CloseReportOptions.CANCEL ? this : null;
	}
	wasClosingCanceled = false;
	return null;
    }

    /**
     * Returns {@link #wasClosingCanceled} value.
     *
     * @return
     */
    public boolean wasClosingCanceled() {
	return wasClosingCanceled;
    }

    @Override
    public void close() {

    }

    @Override
    public String whyCannotClose() {
	return modelBuilder.whyCannotClose();
    }

}
