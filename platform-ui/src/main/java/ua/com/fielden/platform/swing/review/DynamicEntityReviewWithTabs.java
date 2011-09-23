package ua.com.fielden.platform.swing.review;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SingleSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.jvnet.flamingo.common.icon.EmptyResizableIcon;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportView;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportFactory;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportFactoryProvider;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportType;
import ua.com.fielden.platform.swing.analysis.IAnalysisWizardModel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.file.ExtensionFileFilter;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.pagination.Paginator.IPageController;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.view.BasePanel;

import com.jidesoft.swing.JideTabbedPane;

public class DynamicEntityReviewWithTabs<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends DynamicEntityReview<T, DAO, R> {

    private static final long serialVersionUID = -4553413635993473203L;
    protected static final String gridTabName = "Main details";

    private JideTabbedPane viewTabPanel;

    /**
     * Model that holds {@link AbstractAnalysisReportView}s.
     */
    private final Map<String, AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel>> viewTabPanelModel = new HashMap<String, AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel>>();

    private final IAnalysisReportFactoryProvider<T, DAO> analysisReportFactoryProvider;

    /**
     * Holds all details view associated with this instance.
     */
    private final Map<String, Map<Object, DetailsFrame>> detailsCache;

    /**
     * Holds pages of this report.
     */
    private final Map<String, IPage<AbstractEntity>> pageMap = new HashMap<String, IPage<AbstractEntity>>();

    private final String reportName;

    public DynamicEntityReviewWithTabs(//
	    final DynamicEntityReviewModel<T, DAO, R> model, //
	    final boolean showRecords, //
	    final boolean isPrinciple,//
	    final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder, //
	    final IAnalysisReportFactoryProvider<T, DAO> analysisReportFactoryProvider,//
	    final String reportName,//
	    final Map<String, Map<Object, DetailsFrame>> detailsCache) {
	super(model, showRecords, isPrinciple, modelBuilder);

	this.detailsCache = detailsCache;
	this.reportName = reportName;
	this.analysisReportFactoryProvider = analysisReportFactoryProvider;
	viewTabPanel.addTab(gridTabName, getEgiPanel());
	final DynamicEntityQueryCriteria<T, DAO> criteria = getEntityReviewModel().getCriteria();
	final Class<T> clazz = criteria.getEntityClass();

	final Map<String, IAnalysisReportPersistentObject> analysis = modelBuilder.getWizardModel().getAnalysis();
	for (final String name : analysis.keySet()) {
	    final IAnalysisReportPersistentObject analysisItem = analysis.get(name);
	    final IAnalysisReportFactory<T, DAO> analysisReportFactory = analysisReportFactoryProvider.getAnalysisReportFactory(analysisItem.getType());
	    final AbstractAnalysisReportModel<T, DAO> analysisReviewModel = analysisReportFactory.getAnalysisReportModel(getEntityReviewModel(), detailsCache, analysisItem, name, reportName);
	    final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> chartReview = analysisReportFactory.getAnalysisReportView(analysisReviewModel, getReviewContract().getBlockingLayer(), analysisItem);
	    viewTabPanelModel.put(name, chartReview);
	    if (analysisItem.isVisible()) {
		viewTabPanel.addTab(name, chartReview);
	    }
	}

	viewTabPanel.setTabClosableAt(0, false);
	selectTabIndex(0);
	viewTabPanel.setCloseAction(createCloseTabAction());
    }

    /**
     * Selects the specified tab index.
     * 
     * @param index
     */
    private void selectTabIndex(final int index) {
	if (!getEntityReviewModel().isLoadingData()) {
	    viewTabPanel.setSelectedIndex(index);
	}
    }

    /**
     * Selects the specified tab component.
     * 
     * @param comp
     */
    private void selectTabComponent(final Component comp) {
	if (!getEntityReviewModel().isLoadingData()) {
	    viewTabPanel.setSelectedComponent(comp);
	}
    }

    private Action createCloseTabAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = 6001475679820547729L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final int selectedTabIndex = viewTabPanel.getSelectedIndex();
		if (selectedTabIndex > 0) {
		    if (isPrinciple()) {
			removeAnalysisTabSheet(viewTabPanel.getTitleAt(selectedTabIndex));
		    } else {
			final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> chartReview = viewTabPanelModel.get(getSelectedTabTitle());
			if (chartReview != null) {
			    chartReview.setReviewVisible(false);
			}
			viewTabPanel.removeTabAt(selectedTabIndex);
		    }
		}
		if (viewTabPanel.getTabCount() == 0) {
		    viewTabPanel.updateUI();
		}
	    }
	};
    }

    @Override
    protected IReviewContract createReviewContractor() {
	final TabPaneReviewContract reviewContract = new TabPaneReviewContract();
	viewTabPanel = reviewContract.tabPane;
	return reviewContract;
    }

    private JideTabbedPane createTabPanel() {
	final JideTabbedPane tabPane = new JideTabbedPane();
	tabPane.setModel(createTabbedPaneModel());
	tabPane.setHideOneTab(true); // no need to show tab if there is only one
	tabPane.setShowCloseButton(true);
	tabPane.setShowCloseButtonOnTab(true);
	tabPane.setShowCloseButtonOnSelectedTab(true);
	tabPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
	tabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
	tabPane.setBorder(BorderFactory.createLineBorder(new Color(146, 151, 161)));
	return tabPane;
    }

    @Override
    public void saveValues() {
	super.saveValues();
	final Map<String, IAnalysisReportPersistentObject> analysis = new HashMap<String, IAnalysisReportPersistentObject>();
	for (final String name : viewTabPanelModel.keySet()) {
	    final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> tabComponent = viewTabPanelModel.get(name);
	    analysis.put(name, tabComponent.save());
	}
	getModelBuilder().getWizardModel().setAnalysis(analysis);
    }

    @Override
    public ICloseGuard canClose() {
	final ICloseGuard closeGuard = super.canClose();
	if (closeGuard != null) {
	    return closeGuard;
	}
	final JComponent component = getProgressLayer().getView();
	if (component instanceof ICloseGuard) {
	    return ((ICloseGuard) component).canClose();
	}
	return null;
    }

    @Override
    public void close() {
	super.close();
	final JComponent component = getProgressLayer().getView();
	if (component instanceof ICloseGuard) {
	    ((ICloseGuard) component).close();
	}
    }

    /**
     * Returns value that indicates whether tab sheet with specified enteredTabName exists or not.
     * 
     * @param enteredTabName
     * @return
     */
    public boolean existsTab(final String enteredTabName) {
	return (viewTabPanelModel.containsKey(enteredTabName) || gridTabName.equals(enteredTabName));
    }

    /**
     * Selects tab sheet with specified title. If the specified tab is hidden then it opens and selects it.
     * 
     * @param enteredTabName
     *            - specified title of the tab sheet that must be selected.
     */
    public void selectTab(final String enteredTabName) {
	if (gridTabName.equals(enteredTabName)) {
	    selectGridTab();
	    return;
	}

	final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> component = viewTabPanelModel.get(enteredTabName);
	if (component == null) {
	    return;
	}
	final int tabIndex = getTabIndex(enteredTabName);
	if (tabIndex < 0) {
	    component.setReviewVisible(true);
	    viewTabPanel.addTab(enteredTabName, component);
	    selectTabIndex(viewTabPanel.getTabCount() - 1);
	} else {
	    selectTabIndex(tabIndex);
	}
	return;
    }

    /**
     * Selects tab sheet that holds grid details.
     */
    public void selectGridTab() {
	selectTabIndex(0);
    }

    /**
     * Adds new tab sheet to the details view with specified name.
     * 
     * @param name
     */
    public void addNewAnalysisTabSheet(final String name, final IAnalysisReportType reportType) {
	final IAnalysisReportFactory<T, DAO> analysisReportFactory = analysisReportFactoryProvider.getAnalysisReportFactory(reportType);
	final AbstractAnalysisReportModel<T, DAO> analysisReviewModel = analysisReportFactory.getAnalysisReportModel(getEntityReviewModel(), detailsCache, null, name, reportName);
	final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> chartReview = analysisReportFactory.getAnalysisReportView(analysisReviewModel, getReviewContract().getBlockingLayer(), null);
	viewTabPanelModel.put(name, chartReview);
	viewTabPanel.addTab(name, chartReview);
	selectTabComponent(chartReview);
    }

    /**
     * Removes tab sheet associated with specified name.
     * 
     * @param name
     * @return
     */
    public JComponent removeAnalysisTabSheet(final String name) {
	if (gridTabName.equals(name)) {
	    JOptionPane.showMessageDialog(this, "Main details analysis can not be removed", "Informotaion", JOptionPane.INFORMATION_MESSAGE);
	    return null;
	}
	if (getEntityReviewModel().isLoadingData()) {
	    JOptionPane.showMessageDialog(this, "This analysis can not be removed right now.", "Informotaion", JOptionPane.INFORMATION_MESSAGE);
	    return null;
	}
	final BasePanel panelToClose = viewTabPanelModel.get(name);
	if (panelToClose == null) {
	    return null;
	}
	if (panelToClose.canClose() == null) {
	    panelToClose.close();
	} else {
	    JOptionPane.showMessageDialog(panelToClose, panelToClose.whyCannotClose(), "Close tab sheet", JOptionPane.INFORMATION_MESSAGE);
	    return null;
	}
	final int tabIndex = getTabIndex(name);
	if (tabIndex > 0) {
	    viewTabPanel.removeTabAt(tabIndex);
	}
	viewTabPanelModel.remove(name);
	return panelToClose;
    }

    /**
     * Returns index of the tab sheet that is has specified title.
     * 
     * @param name
     * @return
     */
    private int getTabIndex(final String name) {
	for (int tabIndex = 0; tabIndex < viewTabPanel.getTabCount(); tabIndex++) {
	    if (viewTabPanel.getTitleAt(tabIndex).equals(name)) {
		return tabIndex;
	    }
	}
	return -1;
    }

    private SingleSelectionModel createTabbedPaneModel() {
	return new DefaultSingleSelectionModel() {

	    private static final long serialVersionUID = -3273219271999879724L;

	    @Override
	    public void setSelectedIndex(final int index) {
		super.setSelectedIndex(index);
		final String name = viewTabPanel.getTitleAt(viewTabPanel.getSelectedIndex());
		getEntityReviewModel().getPaginator().synchronizeWithView(pageMap.get(name));
		getEntityReviewModel().getExport().setEnabled(gridTabName.equals(name) || viewTabPanelModel.get(name).canExport(), false);
		getEntityReviewModel().getRun().setEnabled(gridTabName.equals(name) || viewTabPanelModel.get(name).canRun(), false);

	    }
	};
    }

    @Override
    protected JComponent buildActionChanger() {
	getEntityReviewModel().getActionChangerBuilder().setAction(createConfigureAnalysisAction());
	final List<String> actionOrder = new ArrayList<String>();
	actionOrder.add("Configure");
	actionOrder.add("Configure analysis");
	actionOrder.add("Save");
	actionOrder.add("Save As");
	if (!isPrinciple()) {
	    actionOrder.add("Delete");
	}
	return getEntityReviewModel().getActionChangerBuilder().buildActionChanger(actionOrder);
    }

    private ActionChanger<?> createConfigureAnalysisAction() {
	return new ActionChanger<Void>("Configure analysis") {

	    private static final long serialVersionUID = 5821607026256750433L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Configure analysis report");
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if (!result) {
		    return false;
		}
		final Result configurationresult = getModelBuilder().getConfigurationController().canConfigureAnalysis(getModelBuilder().getKey());
		if (!configurationresult.isSuccessful()) {
		    JOptionPane.showMessageDialog(DynamicEntityReviewWithTabs.this, configurationresult.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
		    return false;
		}
		return true;
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		final String selectedComponentName = getSelectedTabTitle();
		if (gridTabName.equals(selectedComponentName)) {
		    JOptionPane.showMessageDialog(DynamicEntityReviewWithTabs.this, "Analysis can be configured only when analysis report tab sheet is selected", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> tabComponent = viewTabPanelModel.get(selectedComponentName);
		if (tabComponent != null) {
		    tabComponent.setMode(ReportMode.WIZARD, false);
		}
	    }

	};
    }

    public String getSelectedTabTitle() {
	return viewTabPanel.getTitleAt(viewTabPanel.getSelectedIndex());
    }

    private class TabPaneReviewContract implements IReviewContract {
	/**
	 * Tabbed pane containing detail view grid or chart -- lifecycle or aggregation analyses.
	 */
	private final JideTabbedPane tabPane;

	/**
	 * progress layer that holds tab panel
	 */
	private final BlockingIndefiniteProgressLayer tabPaneProgressLayer;

	public TabPaneReviewContract() {
	    tabPane = createTabPanel();
	    final JPanel basePanel = createTabPanelWrapper();
	    basePanel.add(tabPane, "grow");
	    tabPaneProgressLayer = new BlockingIndefiniteProgressLayer(basePanel, "");
	}

	private JPanel createTabPanelWrapper() {
	    return new BasePanel(new MigLayout("fill, insets 0", "[]", "[]")) {

		private static final long serialVersionUID = -3527476244155742941L;

		@Override
		public String getInfo() {
		    return "no information";
		}

		@Override
		public ICloseGuard canClose() {
		    for (final String chartName : viewTabPanelModel.keySet()) {
			final BasePanel chartReview = viewTabPanelModel.get(chartName);
			final ICloseGuard closeGuard = chartReview.canClose();
			if (closeGuard != null) {
			    selectTab(chartName);
			    return closeGuard;
			}
		    }
		    return null;
		}

		@Override
		public void close() {
		    for (final String chartName : viewTabPanelModel.keySet()) {
			final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> chartReview = viewTabPanelModel.get(chartName);
			chartReview.close();
		    }
		}

	    };
	}

	@Override
	public boolean beforeUpdate() {
	    final String selectedComponentName = tabPane.getTitleAt(tabPane.getSelectedIndex());
	    if (gridTabName.equals(selectedComponentName)) {
		getEntityGridInspector().collapseAllRows();
		getEntityGridInspector().clearSelection();
		return true;
	    } else {
		try {
		    ((AbstractAnalysisReportView<T, DAO, IAnalysisWizardModel, IAnalysisReportModel>) tabPane.getSelectedComponent()).commit();
		} catch (final IllegalStateException e) {
		    JOptionPane.showMessageDialog(tabPane.getSelectedComponent(), e.getMessage(), "Information", JOptionPane.WARNING_MESSAGE);
		    setActionEnabled(true);
		    return false;
		}
		return true;
	    }
	}

	@Override
	public Result getData() {
	    final Result result = getEntityReviewModel().getCriteria().isValid();
	    if (!result.isSuccessful()) {
		return result;
	    }
	    final String selectedComponentName = tabPane.getTitleAt(tabPane.getSelectedIndex());
	    if (gridTabName.equals(selectedComponentName)) {
		// calculate number of rows that would fit into the grid without scrolling
		final int pageSize = getPageSize();
		// need to pause before running actions (all required validation have to be passed)

		return new Result(getEntityReviewModel().getCriteria().run(pageSize > 1 ? pageSize - 1 : pageSize, getEntityReviewModel().getOrderEnhancer()), "Success");
	    } else if (tabPane.getSelectedComponent() instanceof AbstractAnalysisReportView) {
		final AbstractAnalysisReportView<T, DAO, IAnalysisWizardModel, IAnalysisReportModel> selectedAnalysis = (AbstractAnalysisReportView<T, DAO, IAnalysisWizardModel, IAnalysisReportModel>) tabPane.getSelectedComponent();
		return new Result(selectedAnalysis.getModel().runAnalysisQuery(selectedAnalysis.getPageSize()), "Success");
	    } else {
		throw new RuntimeException("Illegal tab selected.");
	    }
	}

	@Override
	public void setDataToView(final Object data) {
	    if (tabPane.getSelectedComponent() instanceof AbstractAnalysisReportView) {
		if (data instanceof IPage) {
		    getEntityReviewModel().getPaginator().setCurrentPage((IPage<AbstractEntity>) data);
		} else {
		    ((AbstractAnalysisReportView) tabPane.getSelectedComponent()).updateView(data, null/*, new IAction() {
												       @Override
												       public void action() {
												       //			    System.out.println("\t\tpostAction getEntityReviewModel().updateState();");
												       getEntityReviewModel().updateState();
												       }
												       }*/);
		}
		((AbstractAnalysisReportView) tabPane.getSelectedComponent()).resetView();
	    } else if (gridTabName.equals(tabPane.getTitleAt(tabPane.getSelectedIndex()))) { //
		getEntityReviewModel().getPaginator().setCurrentPage((IPage<AbstractEntity>) data);
	    } else {
		throw new RuntimeException("Incorrect tab found: " + tabPane.getSelectedComponent());
	    }
	}

	@Override
	public BlockingIndefiniteProgressLayer getBlockingLayer() {
	    return tabPaneProgressLayer;
	}

	@Override
	public boolean canExport() {
	    final String selectedComponentName = tabPane.getTitleAt(tabPane.getSelectedIndex());
	    if (gridTabName.equals(selectedComponentName)) {
		return true;
	    } else {
		final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> analysis = viewTabPanelModel.get(selectedComponentName);
		if (analysis != null) {
		    return analysis.canExport();
		}
		return false;
	    }
	}

	@Override
	public void setActionEnabled(final boolean enable) {
	    getEntityReviewModel().enableButtons(enable);

	    if (!gridTabName.equals(tabPane.getTitleAt(tabPane.getSelectedIndex()))) {
		final AbstractAnalysisReportView view = (AbstractAnalysisReportView) tabPane.getSelectedComponent();
		if (!view.canExport()) {
		    getEntityReviewModel().getExport().setEnabled(false, false);
		}
		if (!view.isPaginationSupport() || !enable) {
		    getEntityReviewModel().getPaginator().disableActions();
		} else {
		    getEntityReviewModel().getPaginator().enableActions();
		}
	    } else {
		if (enable) {
		    getEntityReviewModel().getPaginator().enableActions();
		} else {
		    getEntityReviewModel().getPaginator().disableActions();
		}
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
	    final Result result = getEntityReviewModel().getCriteria().isValid();
	    if (!result.isSuccessful()) {
		return result;
	    }
	    final String selectedComponentName = tabPane.getTitleAt(tabPane.getSelectedIndex());
	    if (gridTabName.equals(selectedComponentName)) {
		return getEntityReviewModel().exportData(file);
	    } else {
		final AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> analysis = viewTabPanelModel.get(selectedComponentName);
		if (analysis != null) {
		    return analysis.exportData(file);
		}
	    }
	    return Result.successful(this);
	}
    }

    @Override
    protected IPageController createPageController() {
	return new IPageController() {

	    @Override
	    public void loadPage(final IPage<?> page) {
		final String tabTitle = viewTabPanel.getTitleAt(viewTabPanel.getSelectedIndex());
		if (gridTabName.equals(tabTitle)) {
		    getEntityReviewModel().getTableModel().clearInstances();
		    ((PropertyTableModel) getEntityReviewModel().getTableModel()).addInstances(page.data().toArray(new AbstractEntity[] {}));
		    getEntityReviewModel().getTableModel().fireTableDataChanged();
		    populateTotalsEditors(page);

		    //getEntityReviewModel().updateState();
		} else {
		    ((AbstractAnalysisReportView<T, DAO, IAnalysisWizardModel, IAnalysisReportModel>) viewTabPanel.getSelectedComponent()).updateView(page.data(), new IAction() {
			@Override
			public void action() {

			    //    getEntityReviewModel().updateState();
			}
		    });
		}
		pageMap.put(tabTitle, (IPage<AbstractEntity>) page);
	    }

	};
    }

}
