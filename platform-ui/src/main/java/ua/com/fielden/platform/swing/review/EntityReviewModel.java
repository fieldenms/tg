/**
 *
 */
package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;
import ua.com.fielden.platform.swing.ei.CriteriaInspectorModel;
import ua.com.fielden.platform.swing.ei.EntityInspectorModel;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.file.ExtensionFileFilter;
import ua.com.fielden.platform.swing.pagination.Paginator;
import ua.com.fielden.platform.swing.pagination.Paginator.IEnableAction;
import ua.com.fielden.platform.swing.pagination.Paginator.IPageChangeFeedback;
import ua.com.fielden.platform.swing.pagination.Paginator.IPageModel;
import ua.com.fielden.platform.swing.sortabletable.PropertyTableModelRowSorter;

import com.jidesoft.grid.TableModelWrapperUtils;

/**
 *
 * Model for the EntityReview panel, defines the logic of the buttons and other components.
 *
 * @author TG Team
 *
 */
public class EntityReviewModel<T extends AbstractEntity, DAO extends IEntityDao<T>, C extends EntityQueryCriteria<T, DAO>> {

    private final PropertyTableModel<T> tableModel;

    private final PropertyTableModelRowSorter<T> sorter;

    private final C criteria;

    private Paginator<AbstractEntity> paginator;

    private BlockingLayerCommand<Result> run;

    private BlockingLayerCommand<Result> export;

    private Command<EntityQueryCriteria<T, DAO>> loadDefaults;

    private final CriteriaInspectorModel<T, DAO, C> criteriaInspectorModel;

    private final List<Runnable> afterRunActions;

    private EntityReview<T, DAO, C> entityReview;

    private final Class<T> entityType;

    private final IPropertyBinder<C> propertyBinder;

    private final IEntityMasterManager emm;

    /**
     * Determines whether data is loading or not.
     */
    private boolean loadingData;

    /**
     * creates new instance of the EntityReviewModel using the specified criteria to retrieve data and table model where the data will be shown and also it creates the
     * {@link EntityInspectorModel} using the criteria and criteriaDetails
     *
     * @param criteria
     * @param builder
     * @param afterRunActions
     *            - actions, which will be fired in {@link Command#postAction()} method of "Run" button
     */
    public EntityReviewModel(final C criteria, final PropertyTableModelBuilder<T> builder, final IEntityMasterManager emm, final Runnable... afterRunActions) {
	this(criteria, builder, null, emm, afterRunActions);
    }

    public EntityReviewModel(final C criteria, final PropertyTableModelBuilder<T> builder, final IPropertyBinder<C> propertyBinder, final IEntityMasterManager emm, final Runnable... afterRunActions) {
	this.criteria = criteria;
	this.propertyBinder = propertyBinder;
	this.entityType = builder.getEntityClass();
	this.emm = emm;
	this.tableModel = enhanceColumnsWithTotals(builder);
	this.sorter = new PropertyTableModelRowSorter<T>(tableModel);
	this.loadDefaults = createLoadDefaults(criteria);
	this.criteriaInspectorModel = createInspectorModel(criteria);
	this.afterRunActions = afterRunActions != null ? new ArrayList<Runnable>(Arrays.asList(afterRunActions)) : new ArrayList<Runnable>();
    }

    public final IEntityMasterManager getEntityMasterFactory() {
	return emm;
    }

    /**
     * Returns value that indicates whether data is loading or not.
     *
     * @return
     */
    public boolean isLoadingData() {
	return loadingData;
    }

    private PropertyTableModel<T> enhanceColumnsWithTotals(final PropertyTableModelBuilder<T> builder) {
	for (final AbstractPropertyColumnMapping mapping : builder.getPropertyColumnMappings()) {
	    if (getCriteria().isTotalsPresent(mapping.getPropertyName())) {
		mapping.setColumnTotals(ColumnTotals.GRAND_TOTALS_SEPARATE_FOOTER);
	    }
	}
	return builder.build(new ArrayList<T>());
    }

    /**
     * Should return model specific action panel builder.
     * <p>
     * Useful when custom actions such as opening of a master frame are required for the entity review.
     * */
    public ActionPanelBuilder getActionPanelBuilder() {
	return null;
    }

    public final PropertyTableModel<T> getTableModel() {
	return tableModel;
    }

    public final Class<T> getEntityType() {
	return entityType;
    }

    /** A convenient method for accessing selected in EGI entity. */
    public T getSelectedEntity() {
	int selectedRow = TableModelWrapperUtils.getActualRowAt(getTableModel().getEntityGridInspector().getModel(), getTableModel().getEntityGridInspector().getSelectedRow());
	if (selectedRow == -1 && getTableModel().instances().size() > 0) {
	    selectedRow = 0;
	}

	return selectedRow < 0 ? null : getTableModel().instance(selectedRow);
    }

    /** A convenient method for accessing selected in EGI entities. */
    public final List<T> getSelectedEntities() {
	return getTableModel().getSelectedEntities();
    }

    private Command<EntityQueryCriteria<T, DAO>> createLoadDefaults(final EntityQueryCriteria<T, DAO> criteria) {
	final Command<EntityQueryCriteria<T, DAO>> action = new Command<EntityQueryCriteria<T, DAO>>("Default") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected EntityQueryCriteria<T, DAO> action(final ActionEvent e) throws Exception {
		criteria.defaultValues();
		return criteria;
	    }

	};
	action.setEnabled(criteria.isDefaultEnabled());
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	action.putValue(Action.SHORT_DESCRIPTION, "Loads default selection criteria");
	return action;
    }

    private String[] getOrderingString() {
	final List<String> orderingString = new ArrayList<String>();
	for (final SortKey key : sorter.getSortKeys()) {
	    orderingString.add(getPropertyAlias(key.getColumn()) + " " + getHqlSuffix(key.getSortOrder()));
	}
	System.out.println("ordering string == " + Arrays.toString(orderingString.toArray(new String[orderingString.size()])));
	return orderingString.toArray(new String[orderingString.size()]);
    }

    private String getHqlSuffix(final SortOrder sortOrder) {
	switch (sortOrder) {
	case ASCENDING:
	    return "asc";
	case DESCENDING:
	    return "desc";
	default:
	    return "";
	}
    }

    private String getPropertyAlias(final int column) {
	final AbstractPropertyColumnMapping<T> columnMapping = tableModel.getPropertyColumnMappings().get(column);
	// TODO : if property is of "AE type" - it should be sorted by key. But what should we do when key of that "AE type" is
	// of AE type too? And so on?.. It should be sorted by ".key.key..." and all necessary sub-properties should be joined -
	// not trivial problem. Furthermore on the way could be composite key.. So this logic should be improved.
	if (AbstractEntity.class.isAssignableFrom(columnMapping.getColumnClass())) {
	    return (!isEmpty(criteria.getAlias()) ? (criteria.getAlias() + ".") : "")
	    + (isEmpty(columnMapping.getPropertyName()) ? "key" : (columnMapping.getPropertyName() + ".key"));
	}
	return (!isEmpty(criteria.getAlias()) ? (criteria.getAlias() + ".") : "") + columnMapping.getPropertyName();
    }

    public Command<EntityQueryCriteria<T, DAO>> getLoadDefaults() {
	return loadDefaults;
    }

    public void addAfterRunAction(final Runnable runnable) {
	afterRunActions.add(runnable);
    }

    /**
     * Creates action for the run button.
     *
     * @param layer
     * @param egiScrollPane
     * @param rowHeight
     */
    private void initRun() {
	final IReviewContract reviewContract = getEntityReview().getReviewContract();
	run = new BlockingLayerCommand<Result>("Run", getEntityReview().getProgressLayer()) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		getEntityReview().getProgressLayer().enableIncrementalLocking();
		setMessage("Loading...");
		loadingData = true;
		boolean result = super.preAction();
		if (!result) {
		    return result;
		}
		commitComponents();
		reviewContract.setActionEnabled(false);
		result = reviewContract.beforeUpdate();
		return result;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		return reviewContract.getData();
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (!result.isSuccessful()) {
		    notifyRunError(result);
		} else {
		    reviewContract.setDataToView(result.getInstance()); // note that currently setting data to view and updating buttons state etc. perform in this single IReviewContract implementor method.
		    for (final Runnable runnable : afterRunActions) {
			runnable.run();
		    }
		}
		reviewContract.setActionEnabled(true);
		super.postAction(result);
		loadingData = false;
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);

		reviewContract.setActionEnabled(true);

		for (final Runnable runnable : afterRunActions) {
		    runnable.run();
		}
		super.postAction(null);
	    }
	};

	run.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
	run.putValue(Action.SHORT_DESCRIPTION, "Execute query");
	run.setEnabled(true);
    }

    protected void notifyRunError(final Result result) {
	JOptionPane.showMessageDialog(getEntityReview(), result.getMessage());
    }

    /**
     * Updates the state of nearby dependent components and objects. This could be buttons, layers etc.
     *
     */
    protected void updateState() {
	enableButtons(true);
    }

    /**
     * Commits all committable components. for e.g. - use OnFocusLost auto-completer's. <br>
     * OnKeyTyped checkBoxes and radioButtons do not need to be committed manually.
     */
    public void commitComponents() {
	for (final IPropertyEditor component : getCriteriaInspectorModel().getEditors().values()) {
	    if (component.getEditor() instanceof BoundedValidationLayer) {
		final BoundedValidationLayer bvl = (BoundedValidationLayer) component.getEditor();
		if (bvl.canCommit()) {
		    bvl.commit();
		}
	    }
	}
    }

    /**
     * Export data of the main analysis grid into file.
     *
     * @param file
     * @return
     */
    protected Result exportData(final File file) throws IOException {
	final List<String> propertyNames = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	final List<String> propertyTitles = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	for (final AbstractPropertyColumnMapping<T> mapping : tableModel.getPropertyColumnMappings()) {
	    propertyNames.add(isEmpty(mapping.getPropertyName()) ? "key" : mapping.getPropertyName());
	    propertyTitles.add(mapping.getPropertyTitle());
	}
	final Result result = getCriteria().isValid();
	if (result.isSuccessful()) {
	    criteria.export(file, getOrderEnhancer(), propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
	    return new Result(file, "Success");
	}
	return result;
    }

    /**
     * Creates action for the export button.
     *
     * @param layer
     * @param egiScrollPane
     * @param rowHeight
     */
    private void initExport() {
	final IReviewContract reviewContract = getEntityReview().getReviewContract();
	export = new BlockingLayerCommand<Result>("Export", getEntityReview().getProgressLayer()) {
	    private static final long serialVersionUID = 1L;
	    private String targetFileName;

	    @Override
	    protected boolean preAction() {
		loadingData = true;
		getEntityReview().getProgressLayer().enableIncrementalLocking();
		if (!(super.preAction() && reviewContract.canExport() && reviewContract.beforeUpdate())) {
		    loadingData = false;
		    return false;
		}
		reviewContract.setActionEnabled(false);
		setMessage("Exporting...");
		// commits all committable components. for e.g. - use OnFocusLost auto-completer's.
		// OnKeyTyped checkBoxes and radioButtons do not need to be committed manually.
		commitComponents();
		// let user choose a file for export
		final JFileChooser fileChooser = targetFileName == null ? new JFileChooser()
		: new JFileChooser(targetFileName.substring(0, targetFileName.lastIndexOf(File.separator)));
		final ExtensionFileFilter filter = reviewContract.getExtensionFilter();
		fileChooser.addChoosableFileFilter(filter);

		boolean fileChosen = false;
		// prompt for a file name until the provided file has a correct extension or the save file dialog is cancelled.
		while (!fileChosen) {
		    // Determine which button was clicked to close the dialog
		    switch (fileChooser.showSaveDialog(getEntityReview())) {
		    case JFileChooser.APPROVE_OPTION: // nothing else is relevant
			final File file = fileChooser.getSelectedFile();
			final String ext = ExtensionFileFilter.getExtension(file);
			if (isEmpty(ext)) {
			    targetFileName = file.getAbsolutePath() + "." + reviewContract.getExtension();
			    fileChosen = true;
			} else if (filter.accept(file)) {
			    targetFileName = file.getAbsolutePath();
			    fileChosen = true;
			}
			break;
		    case JFileChooser.CANCEL_OPTION: // Cancel or the close-dialog icon was clicked
			loadingData = false;
			reviewContract.setActionEnabled(true);
			return false;
		    case JFileChooser.ERROR_OPTION: // The selection process did not complete successfully thus promt again
			fileChosen = false;
			break;
		    }

		    // check if file already exists and request override permission
		    if (fileChosen) {
			final File file = new File(targetFileName);
			if (file.exists()) {
			    fileChosen = JOptionPane.showConfirmDialog(getEntityReview(), "The file already exists. Overwrite?", "Export", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
			}
		    }
		}
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		final File file = new File(targetFileName);
		if (!file.exists()) {
		    file.createNewFile();
		}
		return reviewContract.exportData(file);
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (!result.isSuccessful()) {
		    notifyRunError(result);
		} else {
		    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(getEntityReview(), "Saved successfully. Open?", "Export", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
			try {
			    Desktop.getDesktop().open((File) result.getInstance());
			} catch (final IOException e) {
			    JOptionPane.showMessageDialog(getEntityReview(), "Could not open file. Try opening using standard facilities.\n\n" + e.getMessage(), "Export", JOptionPane.WARNING_MESSAGE);
			}
		    }
		}
		reviewContract.setActionEnabled(true);
		super.postAction(result);
		loadingData = false;
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);

		reviewContract.setActionEnabled(true);
		super.postAction(null);
	    }
	};

	export.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
	export.putValue(Action.SHORT_DESCRIPTION, "Export to Excel");
	export.setEnabled(true);
    }

    /**
     * Enables or disables buttons related with actions like run export and other. Override this to be able to enable or disable other buttons.
     *
     * @param enable
     *            - indicates whether enable or disable buttons
     */
    protected void enableButtons(final boolean enable) {
	getRun().setEnabled(enable);
	getExport().setEnabled(enable);
	if (enable) {
	    getLoadDefaults().setEnabled(criteria.isDefaultEnabled());
	} else {
	    getLoadDefaults().setEnabled(false);
	}
    }

    public BlockingLayerCommand<Result> getRun() {
	if (run == null && getEntityReview() != null) {
	    initRun();
	}
	return run;
    }

    public BlockingLayerCommand<Result> getExport() {
	if (export == null && getEntityReview() != null) {
	    initExport();
	}
	return export;
    }

    /**
     * initiates paginator with blocking layer
     *
     * @param layer
     * @param pageModel
     *            TODO
     */
    void initPaginator(final BlockingIndefiniteProgressLayer layer, final IPageModel pageModel, final IPageChangeFeedback feedback) {
	this.paginator = new Paginator<AbstractEntity>(pageModel, feedback, layer, new IEnableAction() {

	    @Override
	    public void enableAction(final boolean enable) {
		enableButtons(enable);
	    }

	});
    }

    public Paginator<AbstractEntity> getPaginator() {
	return paginator;
    }

    public C getCriteria() {
	return criteria;
    }

    /**
     * returns the model for the {@link EntityInspector} instance created earlier
     *
     * @return
     */
    public CriteriaInspectorModel<T, DAO, C> getCriteriaInspectorModel() {
	return criteriaInspectorModel;
    }

    protected CriteriaInspectorModel<T, DAO, C> createInspectorModel(final C criteria) {
	criteria.getProperty("key").setVisible(false);
	criteria.getProperty("desc").setVisible(false);

	return new CriteriaInspectorModel<T, DAO, C>(criteria);
    }

    public PropertyTableModelRowSorter<T> getSorter() {
	return sorter;
    }

    public void setOrder(final String key, final SortOrder sortOrder, final boolean discardPrevious) {
	sorter.setOrder(sortOrder, key, discardPrevious);
    }

    public void setSortable(final String key, final boolean sortable) {
	sorter.setSortable(key, sortable);
    }

    public boolean isSortable(final String key) {
	return sorter.isSortable(key);
    }

    public void initSorterWith(final List<? extends AbstractPropertyColumnMapping<T>> mappings, final List<SortKey> sortKeys, final boolean[] sortable) {
	sorter.initOrderingWith(mappings, sortKeys, sortable);
    }

    public SortOrder getSortOrder(final String key) {
	return sorter.getSortOrder(key);
    }

    public int getOrder(final String key) {
	return sorter.getOrder(key);
    }

    public boolean[] getSortableArray() {
	return sorter.getIsSortable();
    }

    public List<SortKey> getSortKeys() {
	return new ArrayList<SortKey>(sorter.getSortKeys());
    }

    void setEntityReview(final EntityReview<T, DAO, C> entityReview) {
	if (this.entityReview == null) {
	    this.entityReview = entityReview;
	}
    }

    public EntityReview<T, DAO, C> getEntityReview() {
	return entityReview;
    }

    public IOrderEnhancer getOrderEnhancer() {
	return new IOrderEnhancer() {

	    private final String[] orderingString = getOrderingString();

	    @Override
	    public <E extends AbstractEntity> IQueryOrderedModel<E> enhanceWithOrdering(final ICompleted notOrderedQuery, final Class resultType) {
		if (orderingString.length == 0) {
		    return resultType != null ? notOrderedQuery.model(resultType) : notOrderedQuery.model();
		} else {
		    return resultType != null ? notOrderedQuery.orderBy(orderingString).model(resultType) : notOrderedQuery.orderBy(orderingString).model();
		}
	    }

	};
    }

    public IPropertyBinder<C> getPropertyBinder() {
	return propertyBinder;
    }
}
