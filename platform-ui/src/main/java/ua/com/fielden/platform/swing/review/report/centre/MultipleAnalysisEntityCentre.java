package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
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

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.addtabdialog.AddTabDialog;
import ua.com.fielden.platform.swing.addtabdialog.AddTabDialogModel;
import ua.com.fielden.platform.swing.addtabdialog.AddTabOptions;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationView;
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

    public MultipleAnalysisEntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.tabPanel = createReview();
	getReviewProgressLayer().setView(tabPanel);
	layoutComponents();
	//TODO Also must initiate other saved analysis.
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
    protected JToolBar createToolBar() {
	ActionPanelBuilder panelBuilder = new ActionPanelBuilder();
	final JToolBar subToolBar = super.createToolBar();
	if(subToolBar != null){
	    panelBuilder = panelBuilder.addSubToolBar(subToolBar).addSeparator();
	}
	final JToolBar toolBar = panelBuilder//
		.addButton(new AddAnalysisAction(AnalysisType.SIMPLE, "Add analysis", "Add analysis report", ResourceLoader.getIcon("images/chart-add.png"), ResourceLoader.getIcon("images/chart-add.png")))//
		.addButton(new AddAnalysisAction(AnalysisType.PIVOT, "Add pivot analysis", "Add pivot analysis report", ResourceLoader.getIcon("images/table_add.png"), ResourceLoader.getIcon("images/table_add.png")))//
		.buildActionPanel();
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEmptyBorder());
	return toolBar;
    }

    @Override
    protected List<Action> createCustomActionList() {
	final List<Action> customActions = new ArrayList<Action>();
	customActions.add(getConfigureAction());
	customActions.add(createSaveAction());
	customActions.add(createSaveAsAction());
	customActions.add(createRemoveAction());
	return customActions;
    }

    private JideTabbedPane createReview() {
	final JideTabbedPane tabPane = new JideTabbedPane();
	tabPane.setModel(createTabbedPaneModel(tabPane));
	tabPane.setHideOneTab(true); // no need to show tab if there is only one
	tabPane.setShowCloseButton(true);
	tabPane.setShowCloseButtonOnTab(true);
	tabPane.setShowCloseButtonOnSelectedTab(true);
	tabPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
	tabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
	tabPane.setBorder(BorderFactory.createLineBorder(new Color(146, 151, 161)));
	//Initiates first tab with main details (i.e. grid analysis).
	final GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> mainDetails = createGridAnalysis();
	tabPane.addTab(mainDetails.getModel().getName(), mainDetails);

	tabPane.setTabClosableAt(0, false);
	tabPane.setSelectedIndex(0);
	tabPane.setCloseAction(createCloseTabAction(tabPane));
	return tabPane;
    }

    private Action createCloseTabAction(final JideTabbedPane tabPane) {
	return new AbstractAction() {

	    private static final long serialVersionUID = 6001475679820547729L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		//TODO implement close tab sheet.
	    }
	};
    }

    private SingleSelectionModel createTabbedPaneModel(final JideTabbedPane tabPane) {
	return new DefaultSingleSelectionModel() {

	    private static final long serialVersionUID = -3273219271999879724L;

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    @Override
	    public void setSelectedIndex(final int index) {
		if(!getReviewProgressLayer().isLocked()){
		    super.setSelectedIndex(index);
		    final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?, ?, ?> analysis = (AbstractAnalysisConfigurationView)tabPane.getSelectedComponent();
		    setCurrentAnalysisConfigurationView(analysis);
		}else{
		    JOptionPane.showMessageDialog(tabPane, "The " + tabPane.getTitleAt(index) + " analysis can not be selected right now, " +
			    "because there is another action in progress!", "Information", JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	};
    }

    /**
     * Creates main details analysis configuration view.
     *
     * @return
     */
    private GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> createGridAnalysis(){
	final GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> configModel = new GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer>(getModel().getCriteria());
	final GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> gridConfigView = new GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>(configModel, this, getReviewProgressLayer());
	gridConfigView.open();
	return gridConfigView;
    }

    /**
     * Creates simple chart analysis configuration view with specified name.
     *
     * @param name
     * @return
     */
    private ChartAnalysisConfigurationView<T> createChartAnalysis(final String name){
	final ChartAnalysisConfigurationModel<T> configModel = new ChartAnalysisConfigurationModel<T>(getModel().getCriteria(), name);
	final ChartAnalysisConfigurationView<T> configView = new ChartAnalysisConfigurationView<T>(configModel,this, getReviewProgressLayer());
	return configView;
    }

    /**
     * Creates pivot analysis configuration view with specified name.
     *
     * @param name
     * @return
     */
    private PivotAnalysisConfigurationView<T> createPivotAnalysis(final String name){
	final PivotAnalysisConfigurationModel<T> configModel = new PivotAnalysisConfigurationModel<T>(getModel().getCriteria(), name);
	final PivotAnalysisConfigurationView<T> configView = new PivotAnalysisConfigurationView<T>(configModel,this, getReviewProgressLayer());
	return configView;
    }

    private Action createSaveAction() {
	return new AbstractAction("Save") {

	    private static final long serialVersionUID = 8474884103209307717L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getModel().getConfigurationModel().save();
	    }
	};
    }

    private Action createSaveAsAction() {
	return new AbstractAction("Save As") {

	    private static final long serialVersionUID = 6870686264834331196L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getModel().getConfigurationModel().saveAs();
	    }
	};
    }

    private Action createRemoveAction() {
	return getModel().getName() == null ? null : new AbstractAction("Delete") {

	    private static final long serialVersionUID = 8474884103209307717L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getModel().getConfigurationModel().remove();
	    }
	};
    }

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
		return prevRes;
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

	    final int index = tabIndex(addTabDialog.getEnteredTabName());
	    if(index >=0 ){
		tabPanel.setSelectedIndex(index);
	    }else{
		addAnalysis(addTabDialog.getEnteredTabName(), analysisType);
	    }
	}

	/**
	 * Returns the index of the tab with specified name. If tab with specified name doesn't exists then returns negative number.
	 *
	 * @param name - the specified tab name.
	 * @return
	 */
	protected int tabIndex(final String name) {
	    for (int index = 0; index < tabPanel.getTabCount(); index++) {
		if (tabPanel.getTitleAt(index).equals(name)) {
		    return index;
		}
	    }
	    return -1;
	}

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?, ?, ?>> getAnalysisList() {
	final List<AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?, ?, ?>> analysisList = new ArrayList<AbstractAnalysisConfigurationView<T,ICentreDomainTreeManagerAndEnhancer,?,?,?,?>>();
	for(final Component component : tabPanel.getComponents()){
	    analysisList.add((AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?, ?, ?>)component);
	}
	return analysisList;
    }

    @Override
    public void addAnalysis(final String name, final AnalysisType analysisType) {
	AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?, ?, ?> analysis = null;
	switch(analysisType){
	case SIMPLE:
	    analysis = createChartAnalysis(name);
	case PIVOT:
	    analysis = createPivotAnalysis(name);
	}
	if(analysis != null){
	    tabPanel.addTab(analysis.getModel().getName(), analysis);
	    analysis.open();
	}
    }

    @Override
    public void removeAnalysis(final String name) {
	// TODO Auto-generated method stub

    }
}
