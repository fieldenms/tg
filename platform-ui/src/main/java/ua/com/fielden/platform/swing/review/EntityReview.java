/**
 *
 */
package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.IPropertyAggregationFunction;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.file.ExtensionFileFilter;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.pagination.Paginator;
import ua.com.fielden.platform.swing.pagination.Paginator.IPageModel;
import ua.com.fielden.platform.swing.sortabletable.SorterHandler;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * 
 * Panel that contain the table model to view entities and button panel to navigate between pages.
 * 
 * @author TG Team
 */
public abstract class EntityReview<T extends AbstractEntity, DAO extends IEntityDao<T>, C extends EntityQueryCriteria<T, DAO>> extends BasePanel implements IUmViewOwner {
    private static final long serialVersionUID = -1L;

    private final EgiPanel<T> egiPanel;
    private final EntityReviewModel<T, DAO, C> model;
    private final JButton runButton;
    private final JButton exportButton;
    private final JButton defaultButton;
    private final JLabel jlFeedback = new JLabel("Page 0 of 0");
    private final JPanel buttonPanel;
    private final JPanel criteriaPanel;
    private final JToolBar actionPanel;
    private final IReviewContract reviewContract;

    /**
     * This field is used when showRecords parameter in constructor is true and ensures that {@link ComponentEvent#COMPONENT_RESIZED} event will be handled only once
     */
    private boolean handleComponentResizedEvent = false;

    /**
     * Creates new instance of the review panel, also creates button panel for navigation, run and default button. If showRecords is true then the data will be load when the
     * {@link EntityReview} is shown for the first time otherwise the table will be empty
     * 
     * @param model
     * @param showRecords
     */
    public EntityReview(final EntityReviewModel<T, DAO, C> model, final boolean showRecords) {
	this.model = model;
	model.setEntityReview(this);

	this.egiPanel = new EgiPanel<T>(model.getTableModel(), false);
	egiPanel.setEditorsTooltips(createEditorTooltipsMap(model.getTableModel().getPropertyColumnMappings(), model.getCriteria()));

	final SorterHandler<T> sorterHandler = new SorterHandler<T>();
	sorterHandler.install(egiPanel.getEgi(), model.getSorter());

	final Paginator.IPageChangeFeedback feedback = new Paginator.IPageChangeFeedback() {
	    @Override
	    public void feedback(final IPage<?> page) {
		jlFeedback.setText(page != null ? page.toString() : "Page 0 of 0");
	    }

	    @Override
	    public void enableFeedback(final boolean enable) {
		jlFeedback.setEnabled(enable);
	    }
	};

	reviewContract = createReviewContractor();
	model.initPaginator(reviewContract.getBlockingLayer(), createPageController(), feedback);

	// creating the action panel
	if (model.getActionPanelBuilder() != null) {
	    actionPanel = model.getActionPanelBuilder().buildActionPanel();
	    actionPanel.setFloatable(false);
	    actionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	} else {
	    actionPanel = null;
	}

	this.runButton = new JButton(model.getRun());
	this.exportButton = new JButton(model.getExport());
	this.defaultButton = new JButton(model.getLoadDefaults());

	criteriaPanel = createCriteriaPanel(model);
	buttonPanel = createButtonPanel(model);

	layoutComponents();

	if (showRecords) {
	    // yes we should handle first component-resized event
	    handleComponentResizedEvent = true;
	    egiPanel.getEgi().addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(final ComponentEvent e) {
		    // did we handle any component-resized events?
		    if (handleComponentResizedEvent) {
			// no, so this one is first, lets handle it and set flag to indicate that we won't handle any more component-resized events
			handleComponentResizedEvent = false;
			// now it is assumed that egi gained its size, so we can determine page size - running query
			model.getRun().actionPerformed(null);

			// after this handler end its execution, lets remove it from component because it is already not-useful
			final ComponentAdapter refToThis = this;
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				egiPanel.getEgi().removeComponentListener(refToThis);
			    }
			});
		    }
		}
	    });
	}
    }

    /**
     * Creates mapping between property name and aggregation function name, needed for {@link EgiPanel#setEditorsTooltips(Map)} method.
     * 
     * @param mappings
     * @param criteria
     * @return
     */
    private static Map<String, String> createEditorTooltipsMap(final List<? extends AbstractPropertyColumnMapping> mappings, final EntityQueryCriteria criteria) {
	final Map<String, String> propertyTooltipMap = new HashMap<String, String>();
	for (final AbstractPropertyColumnMapping mapping : mappings) {
	    final IPropertyAggregationFunction aggrFunction = criteria.getPropertyAggregationFunctionFor(isEmpty(mapping.getPropertyName()) ? "id" : mapping.getPropertyName());
	    if (aggrFunction != null) {
		propertyTooltipMap.put(mapping.getPropertyName(), aggrFunction.toString());
	    }
	}
	return propertyTooltipMap;
    }

    /**
     * Override this in order to provide specific {@link IPageModel}.
     * 
     * @return
     */
    protected IPageModel createPageController() {
	return new IPageModel() {

	    @Override
	    public void loadPage(final IPage<?> page) {
		getEntityReviewModel().getTableModel().clearInstances();
		((PropertyTableModel) getEntityReviewModel().getTableModel()).addInstances(page.data().toArray(new AbstractEntity[] {}));
		getEntityReviewModel().getTableModel().fireTableDataChanged();
		populateTotalsEditors(page);
	    }

	};
    }

    protected void populateTotalsEditors(final IPage page) {
	for (final AbstractPropertyColumnMapping<T> mapping : getEntityReviewModel().getTableModel().getPropertyColumnMappings()) {
	    final String propertyName = isEmpty(mapping.getPropertyName()) ? "id" : mapping.getPropertyName();
	    final String alias = getEntityReviewModel().getCriteria().getAliasForTotalsProperty(propertyName);
	    if (alias != null) {
		final Object value = page.summary().get(alias);
		final IPropertyAggregationFunction aggrFunction = getEntityReviewModel().getCriteria().getPropertyAggregationFunctionFor(propertyName);
		final Class valueClass = aggrFunction.getReturnedType(mapping.getColumnClass());
		final String totalsStrValue = EntityUtils.toString(value, valueClass);
		final JComponent totalsEditor = egiPanel.getTotalsEditor(mapping.getPropertyName());
		if (totalsEditor instanceof JTextField) {
		    final JTextField editor = (JTextField) totalsEditor;
		    editor.setText(totalsStrValue);
		    editor.setCaretPosition(0);
		    if (Number.class.isAssignableFrom(valueClass) || Money.class.isAssignableFrom(valueClass) || valueClass == int.class || valueClass == double.class) {
			editor.setHorizontalAlignment(JTextField.RIGHT);
		    }
		}
	    }
	}
    }

    public EgiPanel<T> getEgiPanel() {
	return egiPanel;
    }

    protected IReviewContract createReviewContractor() {
	return new EntityGridReviewContract();
    }

    public IReviewContract getReviewContract() {
	return reviewContract;
    }

    /**
     * Override this to provide custom layout for the main components of the Entity review panel.
     */
    protected void layoutComponents() {
	String rowConstraints = "";
	final List<JComponent> components = new ArrayList<JComponent>();

	if (actionPanel != null && actionPanel.getComponentCount() > 0) {
	    rowConstraints += "[fill]";
	    components.add(actionPanel);
	}
	if (criteriaPanel != null) {
	    rowConstraints += "[fill]";
	    components.add(criteriaPanel);
	}
	components.add(buttonPanel);
	components.add(getProgressLayer());
	rowConstraints += "[fill][:400:, fill, grow]";
	setLayout(new MigLayout("fill, insets 5", "[:400:, fill, grow]", rowConstraints));
	for (int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++) {
	    add(components.get(componentIndex), "wrap");
	}
	add(components.get(components.size() - 1));
    }

    /**
     * Creates criteriaPanel for the specified {@link EntityReviewModel} instance. If there are no criteria, then null is returned
     * <p>
     * Override if the default layout is not appropriate.
     * 
     * @param model
     * @return
     */
    protected JPanel createCriteriaPanel(final EntityReviewModel<T, DAO, C> model) {
	final Map<String, IPropertyEditor> editors = model.getCriteriaInspectorModel().getEditors();
	if (editors.isEmpty()) {
	    return null;
	}
	final TaskPanel critPanel = new TaskPanel(new MigLayout("fill, insets 2", "[:100:][:200:]", "[c]"));
	critPanel.setTitle("Selection criteria");
	for (final IPropertyEditor editor : editors.values()) {
	    final String labelAlignment = editor.getEditor() instanceof JScrollPane ? "align left top" : "";
	    final String editorAlignment = (editor.getEditor() instanceof JScrollPane ? "grow" : "growx") + ", wrap";

	    if (editor.getLabel() != null) {
		critPanel.add(editor.getLabel(), labelAlignment);
	    }
	    critPanel.add(editor.getEditor(), editorAlignment);
	}
	return critPanel;

    }

    /**
     * Creates panel with pagination buttons
     */
    protected JPanel createButtonPanel(final EntityReviewModel<T, DAO, C> model) {
	final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[]push[][][][]20[]push[][]", "[c]"));
	buttonPanel.add(defaultButton);

	buttonPanel.add(navButton(model.getPaginator().getFirst()));
	buttonPanel.add(navButton(model.getPaginator().getPrev()));
	buttonPanel.add(navButton(model.getPaginator().getNext()));
	buttonPanel.add(navButton(model.getPaginator().getLast()));
	buttonPanel.add(jlFeedback);
	buttonPanel.add(exportButton);
	buttonPanel.add(runButton);

	return buttonPanel;
    }

    public JButton getExportButton() {
	return exportButton;
    }

    /**
     * Creates non-focusable {@link JButton} for passed action
     * 
     * @param action
     * @return
     */
    protected JButton navButton(final Action action) {
	final JButton button = new JButton(action);
	button.setFocusable(false);
	return button;
    }

    /**
     * Returns {@link EntityGridInspector} containing instances on page
     * 
     * @return
     */
    public EntityGridInspector<T> getEntityGridInspector() {
	return egiPanel.getEgi();
    }

    /**
     * Returns related {@link EntityReviewModel}
     * 
     * @return
     */
    public EntityReviewModel<T, DAO, C> getEntityReviewModel() {
	return model;
    }

    public BlockingIndefiniteProgressLayer getProgressLayer() {
	return reviewContract.getBlockingLayer();
    }

    public JLabel getJlFeedback() {
	return jlFeedback;
    }

    /**
     * returns the run button of the navigation button panel
     * 
     * @return
     */
    public JButton getRunButton() {
	return runButton;
    }

    /**
     * returns the default button of the navigation button panel that loads the default values for the criteria panel editors
     * 
     * @return
     */
    public JButton getDefaultButton() {
	return defaultButton;
    }

    /**
     * returns the panel where the "next", "previous","first",and "last" button must be situated
     * 
     */
    public JPanel getButtonPanel() {
	return buttonPanel;
    }

    /**
     * returns the panel where the controls for editing criteria properties are situated
     * 
     */
    public JPanel getCriteriaPanel() {
	return criteriaPanel;
    }

    /**
     * counts the number of rows in the table those must be shown on the page
     * 
     * @return
     */
    public int getPageSize() {
	final JPanel contentPanel = getProgressLayer().getView() instanceof JPanel ? (JPanel) getProgressLayer().getView() : null;
	double pageSize;
	if (contentPanel != null) {
	    pageSize = egiPanel.getEgiScrollPane().getSize().getHeight() / egiPanel.getEgi().getRowHeight();
	    if (getCriteriaPanel() instanceof TaskPanel && ((TaskPanel) getCriteriaPanel()).isExpanded()) {
		pageSize += ((TaskPanel) getCriteriaPanel()).getCollapsiblePanel().getSize().getHeight() / getEntityGridInspector().getRowHeight();
	    }
	} else {
	    pageSize = 0;
	}

	return (int) Math.floor(pageSize);
    }

    /**
     * Returns the sort keys for the actual column order
     * 
     * @return
     */
    public List<SortKey> getCurrentSortKeyState() {
	final List<SortKey> sortKeys = new ArrayList<SortKey>();
	for (final SortKey key : model.getSortKeys()) {
	    sortKeys.add(new SortKey(egiPanel.getEgi().convertColumnIndexToView(key.getColumn()), key.getSortOrder()));
	}
	return sortKeys;
    }

    public boolean[] getCurrentSortableColumns() {
	final boolean[] isSortable = new boolean[egiPanel.getEgi().getActualModel().getColumnCount()];
	final boolean[] sortableColumns = model.getSortableArray();
	if (sortableColumns == null) {
	    return null;
	}
	for (int counter = 0; counter < sortableColumns.length; counter++) {
	    isSortable[egiPanel.getEgi().convertColumnIndexToView(counter)] = sortableColumns[counter];
	}
	return isSortable;
    }

    /**
     * returns the action panel (tool bar) that was created and customized with addButton method or removeButton
     * 
     * @return
     */
    public JToolBar getActionPanel() {
	return actionPanel;
    }

    private class EntityGridReviewContract implements IReviewContract {

	private final BlockingIndefiniteProgressLayer egiProgressLayer;

	public EntityGridReviewContract() {
	    egiProgressLayer = new BlockingIndefiniteProgressLayer(egiPanel, "Loading");
	}

	@Override
	public boolean beforeUpdate() {
	    egiPanel.getEgi().collapseAllRows();
	    egiPanel.getEgi().clearSelection();
	    return true;
	}

	@Override
	public BlockingIndefiniteProgressLayer getBlockingLayer() {
	    return egiProgressLayer;
	}

	@Override
	public Result getData() {
	    // calculate number of rows that would fit into the grid without scrolling
	    final int pageSize = getPageSize();
	    // need to pause before running actions (all required validation have to be passed)
	    final Result result = getEntityReviewModel().getCriteria().isValid();
	    if (result.isSuccessful()) {
		return new Result(getEntityReviewModel().getCriteria().run(pageSize > 1 ? pageSize - 1 : pageSize, getEntityReviewModel().getOrderEnhancer()), "Success");
	    }
	    return result;
	}

	@Override
	public void setDataToView(final Object data) {
	    if (data instanceof IPage) {
		getEntityReviewModel().getPaginator().setCurrentPage((IPage<AbstractEntity>) data);
	    }
	}

	@Override
	public boolean canExport() {
	    return true;
	}

	@Override
	public void setActionEnabled(final boolean enable) {
	    getEntityReviewModel().enableButtons(enable);
	    if (enable) {
		getEntityReviewModel().getPaginator().enableActions();
	    } else {
		getEntityReviewModel().getPaginator().disableActions();
	    }
	}

	@Override
	public ExtensionFileFilter getExtensionFilter() {
	    return new ExtensionFileFilter("MS Excel (xls)", getExtension());
	}

	@Override
	public String getExtension() {
	    return "xls";
	}

	@Override
	public Result exportData(final File file) throws IOException {
	    return getEntityReviewModel().exportData(file);
	}

    }

    @Override
    public <E extends AbstractEntity> void notifyEntityChange(final E entity) {
	// by default is does nothing
    }
}
