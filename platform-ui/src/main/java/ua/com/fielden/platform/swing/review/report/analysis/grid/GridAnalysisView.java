package ua.com.fielden.platform.swing.review.report.analysis.grid;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.OpenMasterClickAction;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.ResourceLoader;

public class GridAnalysisView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReview<T, CDTME, IAbstractAnalysisDomainTreeManager> implements IUmViewOwner, IBlockingLayerProvider {

    private static final long serialVersionUID = 8538099803371092525L;

    private final EgiPanel<T> egiPanel;

    private final JToolBar toolBar;

    private final Action openMasterWithNewEntityCommand;
    private final Action openMasterAndEditEntityCommand;
    private final Action deleteEntityCommand;

    public GridAnalysisView(final GridAnalysisModel<T, CDTME> model, final GridConfigurationView<T, CDTME> owner) {
	super(model, owner);
	this.egiPanel = createEgiPanel();
	this.openMasterWithNewEntityCommand = createOpenMasterWithNewCommand();
	this.openMasterAndEditEntityCommand = createOpenMasterCommand();
	this.deleteEntityCommand = createDeleteCommand();
	this.toolBar = createToolBar();
	if (getMasterManager() != null) {
	    OpenMasterClickAction.enhanceWithClickAction(egiPanel.getEgi().getActualModel().getPropertyColumnMappings(),//
		    model.getCriteria().getEntityClass(), //
		    getMasterManager(), //
		    this);
	}
	getModel().getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		final List<AbstractEntity<?>> oldSelected = getEnhnacedSelectedEntities();
		egiPanel.setData((IPage<T>) e.getNewPage());
		selectEntities(oldSelected);
	    }
	});

	addHierarchyListener(new HierarchyListener() {
	    @Override
	    public void hierarchyChanged(final HierarchyEvent e) {
	        if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0 && isShowing()) {
	            if (getModel().getDeltaRetriever() != null) {
	        	getModel().getDeltaRetriever().scheduleDeltaRetrieval();
	            }
	        }
	    }
	});
	getModel().getPageHolder().newPage(null);
	this.addSelectionEventListener(createGridAnalysisSelectionListener());

	layoutView();
    }

    protected EgiPanel<T> createEgiPanel() {
	return new EgiPanel<T>(getModel().getCriteria().getEntityClass(), getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer());
    }

    @SuppressWarnings("unchecked")
    @Override
    public GridConfigurationView<T, CDTME> getOwner() {
        return (GridConfigurationView<T, CDTME>)super.getOwner();
    }

    public Action getOpenMasterWithNewEntityCommand() {
	return openMasterWithNewEntityCommand;
    }

    public Action getOpenMasterAndEditEntityCommand() {
	return openMasterAndEditEntityCommand;
    }

    public Action getDeleteEntityCommand() {
	return deleteEntityCommand;
    }

    public final JToolBar getToolBar() {
	return toolBar;
    }

    public final EgiPanel<T> getEgiPanel() {
	return egiPanel;
    }

    @Override
    public GridAnalysisModel<T, CDTME> getModel() {
	return (GridAnalysisModel<T, CDTME>) super.getModel();
    }

    /**
     * Return the blocking layer for the area of the grid analysis.
     */
    @Override
    public BlockingIndefiniteProgressLayer getBlockingLayer() {
	return getOwner().getProgressLayer();
    }

    /**
     * A convenient method for accessing selected and not enhanced entity in EGI.
     *
     */
    public T getSelectedEntity() {
	final T selectedEntity = getEnhancedSelectedEntity();
	return makeNotEnhnaced(selectedEntity);
    }

    /**
     * Returns the list of selected and not enhanced entities in EGI.
     *
     * @return
     */
    public List<T> getSelectedEntities(){
	final List<T> selectedAndNotEnhancedEntities = new ArrayList<>();
	for(final AbstractEntity<?> selectedEntity : getEnhnacedSelectedEntities()){
	    selectedAndNotEnhancedEntities.add(makeNotEnhnaced(selectedEntity));
	}
	return selectedAndNotEnhancedEntities;
    }

    /**
     * Returns all associated entities in a non-enhanced state.
     *
     * @return
     */
    public List<T> getEntities() {
	final PropertyTableModel<AbstractEntity<?>> tableModel = (PropertyTableModel<AbstractEntity<?>>) getEgiPanel().getEgi().getActualModel();
	final List<T> nonEnhancedEntities = new ArrayList<>();
	for(final AbstractEntity<?> selectedEntity : tableModel.instances()){
	    nonEnhancedEntities.add(makeNotEnhnaced(selectedEntity));
	}
	return nonEnhancedEntities;
    }

    @SuppressWarnings("unchecked")
    private T makeNotEnhnaced(final AbstractEntity<?> enhnacedEntity) {
	return enhnacedEntity == null ? null : (T) enhnacedEntity.copy(DynamicEntityClassLoader.getOriginalType(enhnacedEntity.getType()));
    }

    /**
     * A convenient method for accessing selected in EGI entity.
     *
     * @return
     */
    public T getEnhancedSelectedEntity() {
	final PropertyTableModel<T> tableModel = getEgiPanel().getEgi().getActualModel();
	return tableModel.getSelectedEntity();
    }

    /**
     * A convenient method for accessing selected in EGI entity in terms of {@link AbstractEntity}.
     *
     * @return
     */
    public AbstractEntity<?> getEnhancedSelectedAbstractEntity() {
	final PropertyTableModel<AbstractEntity<?>> tableModel = (PropertyTableModel<AbstractEntity<?>>) getEgiPanel().getEgi().getActualModel();
	return tableModel.getSelectedEntity();
    }

    /**
     * Returns the selected entities in EGI.
     *
     * @return
     */
    public List<AbstractEntity<?>> getEnhnacedSelectedEntities(){
	final PropertyTableModel<AbstractEntity<?>> tableModel = (PropertyTableModel<AbstractEntity<?>>) getEgiPanel().getEgi().getActualModel();
	return tableModel.getSelectedEntities();
    }

    /**
     * Selects the entities in EGI.
     *
     * @return
     */
    public void selectEntities(final List<AbstractEntity<?>> entities) {
	final PropertyTableModel<AbstractEntity<?>> tableModel = (PropertyTableModel<AbstractEntity<?>>) getEgiPanel().getEgi().getActualModel();
	for (final AbstractEntity<?> entity : entities) {
	    tableModel.select(entity);
	}
    }

    public void bringToView(final AbstractEntity<?> entity) {
	final PropertyTableModel<AbstractEntity<?>> tableModel = (PropertyTableModel<AbstractEntity<?>>) getEgiPanel().getEgi().getActualModel();
	final int row = tableModel.getRowOf(entity);
	getEgiPanel().getEgi().scrollRectToVisible(getEgiPanel().getEgi().getCellRect(row, 0, true));
    }

    /**
     * Deselects all instances.
     *
     */
    public void deselectAll() {
	final PropertyTableModel<AbstractEntity<?>> tableModel = (PropertyTableModel<AbstractEntity<?>>) getEgiPanel().getEgi().getActualModel();
	tableModel.deselectRows2();
    }

    /**
     * Updates the specified entity in the grid inspector.
     *
     * @param entity
     */
    public void updateEntry(final T entity) {
	new BlockingLayerCommand<T>(null, getBlockingLayer()) {

	    private static final long serialVersionUID = 5213912604843799656L;

	    @Override
	    protected boolean preAction() {
		if (super.preAction()) {
		    enableRelatedActions(false, false);
		    return true;
		}
		return false;
	    }

	    @Override
	    protected T action(final ActionEvent e) throws Exception {
		return getModel().getEntityById(entity.getId());
	    }

	    @Override
	    protected void postAction(final T value) {
		final PropertyTableModel<T> tableModel = getEgiPanel().getEgi().getActualModel();
		if (value != null) {
		    tableModel.refresh(value);
		    final int row = tableModel.getRowOf(value);
		    if (row >= 0) {
			tableModel.selectRow(row);
		    }
		}
		enableRelatedActions(true, false);
		super.postAction(value);
	    };

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		enableRelatedActions(true, false);
	    }
	}.actionPerformed(null);

    }

    @Override
    public <E extends AbstractEntity<?>> void notifyEntityChange(final E entity) {
	if (entity.isPersisted()) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
		    updateEntry((T) entity);
		}
	    });
	}

    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if (getCentre().getCriteriaPanel() != null) {
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	if (!navigate) {
	    getCentre().getPaginator().setEnableActions(enable, !enable);
	}
	getCentre().getExportAction().setEnabled(enable);
	getCentre().getRunAction().setEnabled(enable);
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return null;
    }

    protected JToolBar createToolBar() {
	final ActionPanelBuilder toolBarBuilder = getOwner().getToolbarCustomiser().createToolbar(this);
	return toolBarBuilder == null || toolBarBuilder.isEmpty() ? null : configureToolBar(toolBarBuilder.buildActionPanel());
    }

    /**
     * Configures the the specified tool bar (i. e. set the floatable property, specifies the border).
     *
     * @param toolBar
     * @return
     */
    private JToolBar configureToolBar(final JToolBar toolBar){
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	return toolBar;
    }

    /**
     * A command that creates and opens an entity master frame for the new entity.
     *
     * @return
     */
    protected Command<T> createOpenMasterWithNewCommand() {
	final Command<T> action = new Command<T>("New") {
	    private static final long serialVersionUID = 1L;

	    private IEntityProducer<T> entityProducer;
	    private IEntityMasterManager masterManager;

	    @Override
	    protected boolean preAction() {
		if (super.preAction()) {
		    masterManager = getMasterManager();
		    final Class<T> entityType = getModel().getCriteria().getEntityClass();
		    entityProducer = masterManager != null ? masterManager.getEntityProducer(entityType) : null;
		    return entityProducer != null;
		}
		return false;
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		return entityProducer.newEntity();
	    }

	    @Override
	    protected void postAction(final T entity) {
		masterManager.<T, IEntityDao<T>> showMaster(entity, GridAnalysisView.this);
		super.postAction(entity);
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_1);
	action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/document-new.png"));
	action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/document-new.png"));
	action.putValue(Action.SHORT_DESCRIPTION, "New");
	action.setEnabled(true);
	return action;
    }

    /**
     * A command that creates and opens an entity master frame for the selected in the EGI entity.
     *
     * @return
     */
    protected Command<T> createOpenMasterCommand() {
	final Command<T> action = new BlockingLayerCommand<T>("Edit", getBlockingLayer()) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		setMessage("Opening...");
		if (super.preAction()) {
		    return getMasterManager() != null && getSelectedEntity() != null;
		}
		return false;
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		return getSelectedEntity();
	    }

	    @Override
	    protected void postAction(final T entity) {
		super.postAction(entity);
		if (entity != null) {
		    getMasterManager().<T, IEntityDao<T>> showMaster(entity, GridAnalysisView.this);
		}
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_2);
	action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/document-edit.png"));
	action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/document-edit.png"));
	action.putValue(Action.SHORT_DESCRIPTION, "Edit");
	action.setEnabled(true);
	return action;
    }

    /**
     * A command that removes the selected in the EGI entity.
     *
     * @return
     */
    protected Command<T> createDeleteCommand() {
	final Command<T> action = new BlockingLayerCommand<T>("Delete", getBlockingLayer()) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		final boolean superRes = super.preAction();
		if (!superRes) {
		    return false;
		}
		final T selectedEntity = getEnhancedSelectedEntity();
		if (selectedEntity == null) {//There are no selected entities or there are more then one are selected.
		    return false;
		}
		if (JOptionPane.showConfirmDialog(GridAnalysisView.this, "Entity " + selectedEntity + " will be deleted. Proceed?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
		    return false;
		}
		setMessage("Deleting...");
		return true;
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		final T selectedEntity = getSelectedEntity();
		getModel().getCriteria().delete(selectedEntity);
		getModel().reExecuteAnalysisQuery();
		return selectedEntity;
	    }

	    @Override
	    protected void postAction(final T entity) {
		super.postAction(entity);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Delete");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_3);
	action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/document-delete.png"));
	action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/document-delete.png"));
	return action;
    }

    /**
     * A convenient method for accessing entity master manager.
     *
     * @return
     */
    public final IEntityMasterManager getMasterManager() {
	return getOwner().getOwner().getModel().getMasterManager();
    }

    /**
     * Determines the number of rows in the table those must be shown on the page using the size of the content panel as the basis. If the calculated size is zero then value of 25
     * is returned. This is done to handle cases where calculation happens prior to panel resizing takes place.
     *
     * @return
     */
    protected int getPageSize() {
	if (AnnotationReflector.isTransactionEntity(getModel().getCriteria().getEntityClass())) {
	    // provide unlimited size of the page for "transaction entities"
	    return Integer.MAX_VALUE;
	}
	double pageSize = egiPanel.getSize().getHeight() / EgiPanel.ROW_HEIGHT;
	if (getOwner().getOwner().getCriteriaPanel() != null) {
	    pageSize += getOwner().getOwner().getCriteriaPanel().getSize().getHeight() / EgiPanel.ROW_HEIGHT;
	}
	final int pageCapacity = (int) Math.floor(pageSize);
	return pageCapacity > 1 ? pageCapacity : 1;
    }

    /**
     * Layouts the components of this analysis.
     */
    protected void layoutView() {
	final List<JComponent> components = new ArrayList<JComponent>();
	final StringBuffer rowConstraints = new StringBuffer("");

	//Creates entity centre's tool bar.
	rowConstraints.append(AbstractEntityCentre.addToComponents(components, "[fill]", getToolBar()));
	rowConstraints.append(AbstractEntityCentre.addToComponents(components, "[fill, grow]", getEgiPanel()));

	setLayout(new MigLayout("fill, insets 0", "[fill, grow]", isEmpty(rowConstraints.toString()) ? "[fill, grow]" : rowConstraints.toString()));
	for (int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++) {
	    add(components.get(componentIndex), "wrap");
	}
	add(components.get(components.size() - 1));
    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     *
     * @return
     */
    protected ISelectionEventListener createGridAnalysisSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		//Managing the default, design and custom action changer button enablements.
		getCentre().getDefaultAction().setEnabled(getCentre().getCriteriaPanel() != null);
		if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
		    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(true);
		}
		if (getCentre().getCustomActionChanger() != null) {
		    getCentre().getCustomActionChanger().setEnabled(true);
		}
		//Managing the paginator's enablements.
		getCentre().getPaginator().setEnableActions(true, false);
		//Managing load and export enablements.
		getCentre().getExportAction().setEnabled(true);
		getCentre().getRunAction().setEnabled(true);
	    }
	};
    }

    @Override
    public void close() {
	getModel().stopDeltaRetrievalIfAny();

        super.close();
    }
}
