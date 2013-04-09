package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.addtabdialog.AddTabDialog;
import ua.com.fielden.platform.swing.addtabdialog.AddTabDialogModel;
import ua.com.fielden.platform.swing.addtabdialog.AddTabOptions;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.MultipleAnalysisEntityCentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.utils.ResourceLoader;

import com.jidesoft.swing.JideTabbedPane;

/**
 * {@link AbstractEntityCentre} with more then one analysis view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class MultipleAnalysisEntityCentre<T extends AbstractEntity<?>> extends AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -5686015614708868918L;

    private final JideTabbedPane tabPanel;

    private final Action removeAction;

    private boolean wasSizeChanged, wasAnalysisLoaded, analysisSelected;

    public MultipleAnalysisEntityCentre(final EntityCentreModel<T> model, final MultipleAnalysisEntityCentreConfigurationView<T> owner) {
	super(model, owner);
	this.wasSizeChanged = false;
	this.wasAnalysisLoaded = false;
	this.analysisSelected = false;
	this.removeAction = createRemoveAnalysisAction();
	addComponentListener(createComponentWasResized());
	addPropertyChangeListener(createAfterLoadSelectListener());
	this.tabPanel = createReview();
	getReviewProgressLayer().setView(createTabPanelWrapper(tabPanel));
	layoutComponents();
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleAnalysisEntityCentreConfigurationView<T> getOwner() {
	return (MultipleAnalysisEntityCentreConfigurationView<T>)super.getOwner();
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    @Override
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

    @Override
    public void selectAnalysis(final String name) {
	final IAbstractAnalysisDomainTreeManager analysis = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer().getAnalysisManager(name);
	AnalysisType analysisType = null;
	if(analysis != null){
	    analysisType = determineAnalysisType(analysis);
	}
	showAnalysis(name, analysisType);
    }

    @Override
    public boolean isLoaded() {
        return wasAnalysisLoaded && wasSizeChanged && analysisSelected;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ICloseGuard canClose() {
        for (int analysisIndex = 0; analysisIndex < tabPanel.getTabCount(); analysisIndex++) {
	    final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> analysisView =//
		    (AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?>) tabPanel.getComponentAt(analysisIndex);
	    if (analysisView.canClose() != null) {
		showAnalysis(analysisView.getModel().getName(), null);
		return analysisView;
	    }
	}
        return null;
    }

    @Override
    public void close() {
	this.wasSizeChanged = false;
	this.wasAnalysisLoaded = false;
	this.analysisSelected = false;
	setSize(new Dimension(0, 0));
	super.close();
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return new ConfigureAction(getOwner()) {

	    private static final long serialVersionUID = 9192913271058568265L;

	    {
		putValue(Action.NAME, "Configure");
		putValue(Action.SHORT_DESCRIPTION, "Configure this entity centre");
	    }

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if (!result) {
		    return false;
		}
		final ICloseGuard closeGuard = canClose();
		if (closeGuard != null) {
		    JOptionPane.showMessageDialog(MultipleAnalysisEntityCentre.this, closeGuard.whyCannotClose(), "Warning", JOptionPane.WARNING_MESSAGE);
		    return false;
		}
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		getOwner().getModel().freez();
		return super.action(e);
	    }

	    @Override
	    protected void restoreAfterError() {
		if(getOwner().getModel().isFreezed()){
		    getOwner().getModel().discard();
		}
	    }
	};
    }

    @Override
    protected JToolBar createToolBar() {
	ActionPanelBuilder panelBuilder = new ActionPanelBuilder();
	final JToolBar subToolBar = super.createToolBar();
	if(subToolBar != null){
	    panelBuilder = panelBuilder.addSubToolBar(subToolBar).addSeparator();
	}
	boolean analysisButtonWasAdded = false;
	if (isAnalysisSupported(AnalysisType.SIMPLE)) {
	    panelBuilder = panelBuilder.addButton(new AddAnalysisAction(AnalysisType.SIMPLE, "Add analysis", "Add analysis report", ResourceLoader.getIcon("images/chart-add.png"), ResourceLoader.getIcon("images/chart-add.png")));//
	    analysisButtonWasAdded = true;
	}
	if (isAnalysisSupported(AnalysisType.MULTIPLEDEC)) {
	    panelBuilder = panelBuilder.addButton(new AddAnalysisAction(AnalysisType.MULTIPLEDEC, "Add multiple dec analysis", "Add multiple dec analysis report", ResourceLoader.getIcon("images/coins.png"), ResourceLoader.getIcon("images/coins.png")));//
	    analysisButtonWasAdded = true;
	}
	if (!Finder.findLifecycleProperties(getModel().getCriteria().getEntityClass()).isEmpty() && isAnalysisSupported(AnalysisType.LIFECYCLE) ) {
	    panelBuilder = panelBuilder.addButton(new AddAnalysisAction(AnalysisType.LIFECYCLE, "Add lifecycle analysis", "Add lifecycle report", ResourceLoader.getIcon("images/chart-add.png"), ResourceLoader.getIcon("images/chart-add.png")));//
	    analysisButtonWasAdded = true;
	}
	if (isAnalysisSupported(AnalysisType.PIVOT)) {
	    panelBuilder = panelBuilder.addButton(new AddAnalysisAction(AnalysisType.PIVOT, "Add pivot analysis", "Add pivot analysis report", ResourceLoader.getIcon("images/table_add.png"), ResourceLoader.getIcon("images/table_add.png")));//
	    analysisButtonWasAdded = true;
	}
	if (isAnalysisSupported(AnalysisType.SENTINEL)) {
	    panelBuilder = panelBuilder.addButton(new AddAnalysisAction(AnalysisType.SENTINEL, "Add sentinel analysis", "Add sentinel analysis report", ResourceLoader.getIcon("images/sentinel-add.png"), ResourceLoader.getIcon("images/sentinel-add.png")));//
	    analysisButtonWasAdded = true;
	}
	if(analysisButtonWasAdded){
	    panelBuilder = panelBuilder.addButton(createRemoveAnalysisAction());
	}
	final JToolBar toolBar = panelBuilder.buildActionPanel();
		toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	return toolBar;
    }

    @Override
    protected List<Action> createCustomActionList() {
	final List<Action> customActions = new ArrayList<Action>();
	customActions.add(getConfigureAction());
	customActions.add(getOwner().getSave());
	customActions.add(getOwner().getSaveAs());
	customActions.add(getOwner().getRemove());
	return customActions;
    }

    private boolean isAnalysisSupported(final AnalysisType analysisType){
        return getModel().getAnalysisBuilder().isSupported(analysisType);
    }

    /**
     * Creates new or opens the existing analysis specified with name and type.
     *
     * @param name
     * @param analysisType
     */
    private void showAnalysis(final String name, final AnalysisType analysisType) {
	final int index = tabIndex(tabPanel, name);
	if(index >=0 ){
	    tabPanel.setSelectedIndex(index);
	} else {
	    final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> analysis = createAnalysis(name, analysisType);
	    if(analysis != null){
		tabPanel.addTab(analysis.getModel().getName(), analysis);
		analysis.addLoadListener(new ILoadListener() {

		    @Override
		    public void viewWasLoaded(final LoadEvent event) {
			tabPanel.setSelectedComponent(analysis);
			analysis.removeLoadListener(this);
		    }
		});

		analysis.open();
	    }
	}
    }

    /**
     * Hides the analysis specified with name.
     *
     * @param name
     */
    private void hideAnalysis(final String name) {
	final int tabIndex = tabIndex(tabPanel, name);
	if(canRemoveTabSheet(tabIndex, false)){
	    removeTabSheet(tabIndex);
	}
    }

    /**
     * Creates the {@link HierarchyListener} that determines when the component was shown and it's size was determined.
     * Also if analysis components were also loaded then it fires the load event.
     *
     * @return
     */
    private ComponentListener createComponentWasResized() {
	return new ComponentAdapter() {

	    @Override
	    public void componentResized(final ComponentEvent e) {
		synchronized (MultipleAnalysisEntityCentre.this) {
		    // should size change event be handled?
		    if (!wasSizeChanged) {
			// yes, so this one is first, lets handle it and set flag
			// to indicate that we won't handle any more
			// size changed events
			wasSizeChanged = true;

			//The component was resized so lets see whether analysis was loaded and the active one was selected.
			//If that is true then fire
			//event that this component was loaded.
			if(wasAnalysisLoaded && analysisSelected){
			    fireLoadEvent(new LoadEvent(MultipleAnalysisEntityCentre.this));
			}
		    }
		}
	    }
	};
    }

    /**
     * Creates the after analysis load select listener. This listener fires the load event after the analysis were loaded and the active one was selected.
     *
     * @return
     */
    private PropertyChangeListener createAfterLoadSelectListener() {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		synchronized (MultipleAnalysisEntityCentre.this) {
		    if("currentAnalysisConfigurationView".equals(evt.getPropertyName()) && !analysisSelected && wasAnalysisLoaded && getCurrentAnalysisConfigurationView().getModel().getName().equals(getOwner().getAnalysisToSelect())){

			analysisSelected = true;
			if(wasAnalysisLoaded && wasSizeChanged){
			    fireLoadEvent(new LoadEvent(MultipleAnalysisEntityCentre.this));
			}
		    }
		}
	    }
	};
    }

    /**
     * Returns the analysis type for the specified instance of {@link IAbstractAnalysisDomainTreeManager}.
     *
     * @param analysisManager
     * @return
     */
    private AnalysisType determineAnalysisType(final IAbstractAnalysisDomainTreeManager analysisManager) {
	if (analysisManager instanceof ISentinelDomainTreeManager) {
	    return AnalysisType.SENTINEL;
	} else if (analysisManager instanceof IAnalysisDomainTreeManager) {
	    return AnalysisType.SIMPLE;
	} else if(analysisManager instanceof IPivotDomainTreeManager) {
	    return AnalysisType.PIVOT;
	} else if(analysisManager instanceof ILifecycleDomainTreeManager) {
	    return AnalysisType.LIFECYCLE;
	} else if(analysisManager instanceof IMultipleDecDomainTreeManager) {
	    return AnalysisType.MULTIPLEDEC;
	}
	return null;
    }

    /**
     * Returns the panel that wraps the tab panel with analysis.
     *
     * @param tabPanel
     * @return
     */
    private BasePanel createTabPanelWrapper(final JideTabbedPane tabPanel) {
	final BasePanel tabPanelWrapper = new BasePanel(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow]")) {

	    private static final long serialVersionUID = -6010772291931485281L;

	    @Override
	    public String getInfo() {
		return "Tab pane wrapper";
	    }
	};
	tabPanelWrapper.add(tabPanel);
	return tabPanelWrapper;
    }

    /**
     * Creates the view with grid analysis. Also loads other analysis.
     *
     * @return
     */
    private JideTabbedPane createReview() {
	final JideTabbedPane tabPane = new JideTabbedPane();
	tabPane.addChangeListener(new ChangeListener() {

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    @Override
	    public void stateChanged(final ChangeEvent e) {
		final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> analysis = (AbstractAnalysisConfigurationView) tabPane.getSelectedComponent();
		setCurrentAnalysisConfigurationView(analysis);
	    }
	});
	tabPane.setModel(createTabbedPaneModel(tabPane));
	tabPane.setHideOneTab(true); // no need to show tab if there is only one
	tabPane.setShowCloseButton(true);
	tabPane.setShowCloseButtonOnTab(true);
	tabPane.setShowCloseButtonOnSelectedTab(true);
	tabPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
	tabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
	tabPane.setBorder(BorderFactory.createLineBorder(new Color(146, 151, 161)));
	//Initiates first tab with main details (i.e. grid analysis).
	final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> mainDetails = createAnalysis(null, null);
	addLoadAnalysisListenerTo(tabPane, mainDetails, getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer().analysisKeys(), 0);
	tabPane.addTab(mainDetails.getModel().getName(), mainDetails);
	tabPane.setTabClosableAt(0, false);
	tabPane.setCloseAction(createCloseTabAction());
	mainDetails.open();
	return tabPane;
    }

    /**
     * Adds {@link ILoadListener} to the specified analysis configuration view and loads next visible analysis.
     * @param tabPanel
     *
     * @param configView
     * @param analysisKeys
     * @param analysisIndex
     */
    private void addLoadAnalysisListenerTo(final JideTabbedPane tabPane, final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> configView, final List<String> analysisKeys, final int analysisIndex) {
	if(configView == null){
	    loadNextAnalysis(tabPane, analysisKeys, analysisIndex);
	}else{
	    configView.addLoadListener(new ILoadListener() {

		@Override
		public void viewWasLoaded(final LoadEvent event) {
		    loadNextAnalysis(tabPane, analysisKeys, analysisIndex);
		}
	    });
	}
    }

    /**
     * Loads the analysis specified with analysis index in the analysisKeys list.
     * @param tabPane
     *
     * @param analysisKeys
     * @param analysisIndex
     */
    private synchronized void loadNextAnalysis(final JideTabbedPane tabPane, final List<String> analysisKeys, final int analysisIndex) {
	final int visibleAnalysisIndex = findFirstVisibleAnalysis(analysisKeys, analysisIndex);
	if(analysisKeys.size() > visibleAnalysisIndex){
	    final String name = analysisKeys.get(visibleAnalysisIndex);
	    final AnalysisType type = determineAnalysisType(getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer().getAnalysisManager(name));
	    final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> newAnalysis = createAnalysis(name, type);
	    addLoadAnalysisListenerTo(tabPane, newAnalysis, analysisKeys, visibleAnalysisIndex + 1);
	    if(newAnalysis != null){
		tabPane.addTab(newAnalysis.getModel().getName(), newAnalysis);
		newAnalysis.open();
	    }
	} else {
	    wasAnalysisLoaded = true;
	    if(getCurrentAnalysisConfigurationView() != null && getCurrentAnalysisConfigurationView().getModel().getName().equals(getOwner().getAnalysisToSelect())){
		analysisSelected = true;
		fireLoadEvent(new LoadEvent(this));
	    } else {
		final int tabIndex = tabIndex(tabPane, getOwner().getAnalysisToSelect());
		if(tabIndex >=0 ){
		    tabPane.setSelectedIndex(tabIndex);
		}
	    }
	}
    }

    private IAbstractConfigurationViewEventListener createCancelCloseListener(final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> newAnalysis) {
	return new IAbstractConfigurationViewEventListener() {

	    @Override
	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
		switch(event.getEventAction()){
		case PRE_CANCEL:
		    if(newAnalysis.isNewConfigurationView()){
			removeAction.actionPerformed(null);
			return new Result(new Exception("Can not cancel first time open analysis"));
		    }
		    break;
		default:
		    break;
		}
		return Result.successful(event.getSource());
	    }
	};
    }

    /**
     * Returns an index of the first visible analysis.
     *
     * @param analysisKeys
     * @param analysisIndex
     * @return
     */
    private int findFirstVisibleAnalysis(final List<String> analysisKeys, final int analysisIndex){
	final ICentreDomainTreeManagerAndEnhancer centreManager = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();
	int index = analysisIndex;
	for(; index < analysisKeys.size(); index++){
	    final IAbstractAnalysisDomainTreeManager analysisManager = centreManager.getAnalysisManager(analysisKeys.get(index));
	    if(analysisManager.isVisible() || analysisKeys.get(index).equals(getOwner().getAnalysisToSelect())){
		return index;
	    }
	}
	return index;
    }

    /**
     * Creates the analysis configuration view for the specified name and type.
     *
     * @param name - the analysis name.
     * @param type - the analysis type.
     * @return
     */
    private AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> createAnalysis(final String name, final AnalysisType type){
	final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> newAnalysis = getModel().getAnalysisBuilder()//
		.createAnalysis(type, name, getOwner().getDetailsCache(name), this, getModel().getCriteria(), getReviewProgressLayer());
	newAnalysis.addConfigurationEventListener(createCancelCloseListener(newAnalysis));
	return newAnalysis;
    }

    /**
     * Returns the close tab action.
     *
     * @return
     */
    private Action createCloseTabAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = 6001475679820547729L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		closeCurrentAnalysis();
	    }
	};
    }

    /**
     * Closes current analysis.
     */
    private void closeCurrentAnalysis(){
	if(getModel().getName() == null){
	    removeAction.actionPerformed(null);
	}else{
	    hideAnalysis(getCurrentAnalysisConfigurationView().getModel().getName());
	}
    }

    /**
     * Creates and returns the action that removes currently selected analysis.
     *
     * @return
     */
    private Action createRemoveAnalysisAction() {
	return new Command<Void>("Remove  analysis") {
	    private static final long serialVersionUID = -1271728862598382645L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Remove selected analysis");
		putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/chart-remove.png"));
		putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/chart-remove.png"));
	    }

	    @Override
	    protected boolean preAction() {
		if(super.preAction()){
		    return canRemoveTabSheet(tabPanel.getSelectedIndex(), true);
		}
		return false;
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		final AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> analysisModel = getCurrentAnalysisConfigurationView().getModel();
		if(analysisModel.isFreeze()){
		    analysisModel.discard();
		}
		analysisModel.remove();
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		removeTabSheet(tabPanel.getSelectedIndex());
		super.postAction(value);
	    }
	};
    }

    /**
     * Determines whether analysis specified with tab sheet index can be removed.
     *
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean canRemoveTabSheet(final int index, final boolean remove){
	if(index < 0){
	    throw new IllegalArgumentException("The tab index can not be less then 0");
	}
	final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> analysis = (AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?>)tabPanel.getComponentAt(index);
	if(analysis instanceof GridConfigurationView){
	    JOptionPane.showMessageDialog(this, "Main details analysis can not be removed", "Informotaion", JOptionPane.WARNING_MESSAGE);
	    return false;
	}
	if(getReviewProgressLayer().isLocked()){
	    JOptionPane.showMessageDialog(this, "This analysis can not be removed right now.", "Informotaion", JOptionPane.WARNING_MESSAGE);
	    return false;
	}
	if(!remove && analysis.canClose() != null){
	    JOptionPane.showMessageDialog(this, getCurrentAnalysisConfigurationView().whyCannotClose(), "Close tab sheet", JOptionPane.WARNING_MESSAGE);
	    return false;
	}
	return true;
    }

    /**
     * Returns the removed analysis.
     *
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    private AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> removeTabSheet(final int index){
	if(index < 0){
	    throw new IllegalArgumentException("The tab index can not be less then 0");
	}
	final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> analysis = (AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?>)tabPanel.getComponentAt(index);
	analysis.getModel().setAnalysisVisible(false);
	tabPanel.removeTabAt(index);
	analysis.close();
	return analysis;
    }

    /**
     * Creates the {@link SingleSelectionModel} for the specified tab panel.
     *
     * @param tabPane
     * @return
     */
    private SingleSelectionModel createTabbedPaneModel(final JideTabbedPane tabPane) {
	return new DefaultSingleSelectionModel() {

	    private static final long serialVersionUID = -3273219271999879724L;

	    @Override
	    public void setSelectedIndex(final int index) {
		if(!isLoaded() || !getReviewProgressLayer().isLocked()){
		    super.setSelectedIndex(index);
		}
	    }
	};
    }

    /**
     * Returns the index of the tab with specified name. If tab with specified name doesn't exists then returns negative number.
     * @param tabPane
     *
     * @param name - the specified tab name.
     * @return
     */
    private static int tabIndex(final JideTabbedPane tabPane, final String name) {
	for (int index = 0; index < tabPane.getTabCount(); index++) {
	    if (tabPane.getTitleAt(index).equals(name)) {
		return index;
	    }
	}
	return -1;
    }

    /**
     * The action that is responsible for creating or opening analysis.
     *
     * @author TG Team
     *
     */
    private class AddAnalysisAction extends Command<Void>{

	private static final long serialVersionUID = 1916078804942683757L;

	private final AddTabDialog addTabDialog;

	private final AnalysisType analysisType;

	public AddAnalysisAction(final AnalysisType analysisType, final String name, final String shortDescription, final Icon largeIcon, final Icon smallIcon){
	    super(name);
	    putValue(Action.SHORT_DESCRIPTION, shortDescription);
	    putValue(Action.LARGE_ICON_KEY, largeIcon);
	    putValue(Action.SMALL_ICON, smallIcon);
	    this.addTabDialog = new AddTabDialog(new AddTabDialogModel());
	    this.analysisType = analysisType;
	}

	@Override
	protected final boolean preAction() {
	    final boolean prevRes = super.preAction();
	    if (!prevRes) {
		return false;
	    }
	    if (getReviewProgressLayer().isLocked()) {
		JOptionPane.showMessageDialog(MultipleAnalysisEntityCentre.this, "This analysis can not be added right now.", "Informotaion", JOptionPane.WARNING_MESSAGE);
		return false;
	    }
	    return AddTabOptions.ADD_TAB.equals(addTabDialog.showDialog(SwingUtilities.getWindowAncestor(MultipleAnalysisEntityCentre.this)));
	}

	@Override
	protected final Void action(final ActionEvent e) throws Exception {
	    return null;
	}

	@Override
	protected final void postAction(final Void value) {
	    super.postAction(value);
	    showAnalysis(addTabDialog.getEnteredTabName(), analysisType);
	}
    }
}
