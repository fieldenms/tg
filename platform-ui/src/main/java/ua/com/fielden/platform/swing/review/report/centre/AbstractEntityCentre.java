package ua.com.fielden.platform.swing.review.report.centre;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import net.miginfocom.swing.MigLayout;

import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.icon.EmptyResizableIcon;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.ActionChangeButton;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.pagination.development.Paginator;
import ua.com.fielden.platform.swing.pagination.development.Paginator.IPageChangeFeedback;
import ua.com.fielden.platform.swing.pagination.model.development.IPageHolderManager;
import ua.com.fielden.platform.swing.pagination.model.development.PaginatorModel;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;

/**
 * Implements common functionality for all types of entity centres: entity centre with single analysis, entity locators, entity centres with multiple analysis.
 * When extending this class one must remember to layout components. It may be done using utility methods of the {@link EntityCentreLayoutUtility} class.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractEntityCentre<T extends AbstractEntity> extends AbstractEntityReview<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = -6079569752962700417L;

    //progress layer for review.
    private final BlockingIndefiniteProgressLayer reviewProgressLayer;

    //Main parts of the entity centre
    private final JToolBar toolBar;
    private final StubCriteriaPanel criteriaPanel;
    private final JPanel actionPanel;

    //Paginator related properties
    private final IPageHolderManager pageHolderManager;
    private final JLabel feedBack;
    private final Paginator paginator;

    //control panel actions (i.e. run, export, default actions)
    private final JComponent customActionChanger;
    private final Action defaultAction;
    private final Action exportAction;
    private final Action runAction;

    //Holds current operable analysis report.
    private AbstractAnalysisConfigurationView<T, ?, ?, ?, ?> currentAnalysisConfigurationView;

    /**
     * Initiates this {@link AbstractEntityCentre}. Creates all parts and components of entity centre.
     * 
     * @param model
     * @param progressLayer
     */
    public AbstractEntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	//Initiates the paginator related properties.
	final PaginatorModel paginatorModel = new PaginatorModel();
	this.reviewProgressLayer = new BlockingIndefiniteProgressLayer(null, "");
	this.feedBack = new JLabel("Page 0 of 0");
	this.pageHolderManager = paginatorModel;
	this.paginator = new Paginator(paginatorModel, createPaginatorFeedback(), reviewProgressLayer);
	//Initiates control panel actions
	this.defaultAction = createDefaultAction();
	this.exportAction = createExportAction();
	this.runAction = createRunAction();
	this.customActionChanger = createCustomActionButton();
	//Initiates the main parts of the entity centre.
	this.toolBar = createToolBar();
	this.criteriaPanel = createCriteriaPanel();
	this.actionPanel = createControlPanel();

	addPropertyChangeListener(createCurrentAnalysisChangeListener());
    }

    @Override
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

    /**
     * Returns the currently operable analysis report.
     * 
     * @return
     */
    public final AbstractAnalysisConfigurationView<T, ?, ?, ?, ?> getCurrentAnalysisConfigurationView() {
	return currentAnalysisConfigurationView;
    }

    /**
     * 
     * 
     * @return
     */
    public final IPageHolderManager getPageHolderManager() {
	return pageHolderManager;
    }

    /**
     * Returns action for the button that allows one to set default values.
     * 
     * @return
     */
    public final Action getDefaultAction() {
	return defaultAction;
    }

    /**
     * Returns the component that might be a {@link JButton} or {@link ActionChangeButton} instance. This component allows user to save or modify entity centre.
     * 
     * @return
     */
    public JComponent getCustomActionChanger() {
	return customActionChanger;
    }

    /**
     * Returns an action that initiates data loading process.
     * 
     * @return
     */
    public final Action getRunAction() {
	return runAction;
    }

    /**
     * Returns an action that initiates data exporting process.
     * 
     * @return
     */
    public final Action getExportAction() {
	return exportAction;
    }

    /**
     * Returns the {@link Paginator} associated with this entity review.
     * 
     * @return
     */
    public final Paginator getPaginator() {
	return paginator;
    }

    /**
     * Returns the progress layer for the review panel.
     * 
     * @return
     */
    public final BlockingIndefiniteProgressLayer getReviewProgressLayer() {
	return reviewProgressLayer;
    }

    /**
     * Returns tool bar for entity centre.
     * 
     * @return
     */
    public final JToolBar getToolBar() {
	return toolBar;
    }

    /**
     * Returns the selection criteria panel.
     * 
     * @return
     */
    public final StubCriteriaPanel getCriteriaPanel() {
	return criteriaPanel;
    }

    /**
     * Returns the control panel with controls those are used for data loading, exporting and configuring the whole centre.
     * 
     * @return
     */
    public final JPanel getActionPanel() {
	return actionPanel;
    }

    /**
     * Returns the review panel of the entity centre. This panel must displays the data loading result.
     * 
     * @return
     */
    public abstract JComponent getReviewPanel();

    /**
     * Override this to provide custom tool bar.
     * 
     * @return
     */
    protected JToolBar createToolBar() {
	return null;
    }

    /**
     * Override this to provide custom criteria panel (i.e. panel with controls binded to the entity, that specifies the criteria).
     * 
     * @return
     */
    protected StubCriteriaPanel createCriteriaPanel() {
	return null;
    }

    /**
     * Set the analysis report that must be operable.
     * 
     * @param currentAnalysisConfigurationView
     */
    protected final void setCurrentAnalysisConfigurationView(final AbstractAnalysisConfigurationView<T, ?, ?, ?, ?> currentAnalysisConfigurationView) {
	final AbstractAnalysisConfigurationView<T, ?, ?, ?, ?> oldAnalysisConfigurationView = this.currentAnalysisConfigurationView;
	this.currentAnalysisConfigurationView = currentAnalysisConfigurationView;
	firePropertyChange("currentAnalysisConfigurationView", oldAnalysisConfigurationView, this.currentAnalysisConfigurationView);
    }

    /**
     * Layouts the main parts and components of this entity centre.
     * 
     */
    protected void layoutComponents(){
	final List<JComponent> components = new ArrayList<JComponent>();
	final StringBuffer rowConstraints = new StringBuffer("");

	//Creates entity centre's tool bar.
	rowConstraints.append(addToComponents(components, "[fill]", getToolBar()));

	//creates the criteria panel for entity centre
	final TaskPanel taskPanel = getCriteriaPanel() == null ? null : new TaskPanel(new MigLayout("fill, insets 0"));
	if(taskPanel != null){
	    taskPanel.add(getCriteriaPanel(), "grow, wrap");
	    taskPanel.setTitle("Selection criteria");
	    taskPanel.setAnimated(false);
	}
	rowConstraints.append(addToComponents(components, "[fill]", taskPanel));

	//Creates and initiates control panel.
	rowConstraints.append(addToComponents(components, "[fill]", getActionPanel()));
	//Creates the centre's review.
	rowConstraints.append(addToComponents(components, "[:400:, fill, grow]", getReviewPanel()));

	removeAll();
	setLayout(new MigLayout("fill, insets 5", "[:400:, fill, grow]", isEmpty(rowConstraints.toString()) ? "[fill, grow]" : rowConstraints.toString()));

	for(int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++){
	    add(components.get(componentIndex), "wrap");
	}
	add(components.get(components.size()-1));
    }

    protected String addToComponents(final List<JComponent> components, final String constraint, final JComponent component) {
	if(component != null){
	    components.add(component);
	    return constraint;
	}
	return "";
    }

    /**
     * Returns action for the button that allows one to set default values.
     * 
     * @return
     */
    private Action createDefaultAction(){
	return new Command<Void>("Default") {

	    private static final long serialVersionUID = -1287083156047119434L;

	    {
		setEnabled(getModel().getCriteria().isDefaultEnabled());
		putValue(MNEMONIC_KEY, KeyEvent.VK_D);
		putValue(SHORT_DESCRIPTION, "Loads default selection criteria");
	    }

	    @Override
	    protected boolean preAction() {
		if(ReportMode.REPORT != getCurrentAnalysisConfigurationView().getModel().getMode()){
		    throw new IllegalStateException("This action shouldn't be invoked when analysis is in WIZARD or not specified mode.");
		}
		return super.preAction() && getModel().getCriteria().isDefaultEnabled();
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().getCriteria().defaultValues();
		return null;
	    }

	};
    }

    /**
     * Returns an action that initiates data loading process.
     * 
     * @return
     */
    private Action createRunAction(){
	return new AbstractAction("Run") {

	    private static final long serialVersionUID = -3516438577329734057L;

	    {
		putValue(MNEMONIC_KEY, KeyEvent.VK_R);
		putValue(SHORT_DESCRIPTION, "Execute query");
		setEnabled(true);
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		try{
		    if(ReportMode.REPORT != getCurrentAnalysisConfigurationView().getModel().getMode()){
			throw new IllegalStateException("This action shouldn't be invoked when analysis is in WIZARD or not specified mode.");
		    }
		    getCurrentAnalysisConfigurationView().getPreviousView().loadData();
		}catch(final IllegalStateException exception){
		    new DialogWithDetails(null, "Exception in action", exception).setVisible(true);
		}
	    }
	};
    }

    /**
     * Returns an action that initiates data exporting process.
     * 
     * @return
     */
    private Action createExportAction(){
	return new AbstractAction("Export") {

	    private static final long serialVersionUID = -3516438577329734057L;

	    {
		putValue(MNEMONIC_KEY, KeyEvent.VK_E);
		putValue(SHORT_DESCRIPTION, "Export data");
		setEnabled(true);
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		try{
		    if(ReportMode.REPORT != getCurrentAnalysisConfigurationView().getModel().getMode()){
			throw new IllegalStateException("This action shouldn't be invoked when analysis is in WIZARD or not specified mode.");
		    }
		    getCurrentAnalysisConfigurationView().getPreviousView().exportData();
		}catch(final IllegalStateException exception){
		    new DialogWithDetails(null, "Exception in action", exception).setVisible(true);
		}
	    }
	};

    }

    /**
     * Creates and returns the panel that contains default, run, export and other buttons those operate on criteria or review panel.
     * 
     * @return
     */
    private JPanel createControlPanel() {

	final List<JComponent> controlButtons = new ArrayList<JComponent>();
	final StringBuffer columnConstraints = new StringBuffer("");

	if(getCriteriaPanel() != null && getCriteriaPanel().canConfigure()){
	    columnConstraints.append("[70::,fill]");
	    controlButtons.add(new JToggleButton(getCriteriaPanel().getSwitchAction()));
	}
	columnConstraints.append(addToComponents(controlButtons, "[160::,fill]", getCustomActionChanger()));

	final JPanel controlPanel = new JPanel(new MigLayout("fill, insets 0", "[70::,fill]" + columnConstraints.toString() + "20:push[][][][]20[]push[:70:,fill][:70:,fill]", "[c,fill]"));

	controlPanel.add(newButton(getDefaultAction()));
	for(final JComponent component : controlButtons){
	    controlPanel.add(component);
	}

	controlPanel.add(newButton(getPaginator().getFirst()));
	controlPanel.add(newButton(getPaginator().getPrev()));
	controlPanel.add(newButton(getPaginator().getNext()));
	controlPanel.add(newButton(getPaginator().getLast()));
	controlPanel.add(feedBack);

	controlPanel.add(newButton(getExportAction()));
	controlPanel.add(newButton(getRunAction()));

	return controlPanel;

    }

    /**
     * Creates and returns the {@link IPageChangeFeedback} implementation for this entity centre needed
     * 
     * @return
     */
    private IPageChangeFeedback createPaginatorFeedback() {
	return new IPageChangeFeedback() {
	    @Override
	    public void feedback(final IPage<?> page) {
		feedBack.setText(page != null ? page.toString() : "Page 0 of 0");
	    }

	    @Override
	    public void enableFeedback(final boolean enable) {
		feedBack.setEnabled(enable);
	    }
	};
    }

    /**
     * Returns the button that may contain custom actions: configure, save, save as, remove and other actions)
     * 
     * @return
     */
    private JComponent createCustomActionButton() {
	//Initiates the list of all review actions (i.e. configure, save, save as, save as default, and remove actions)
	//TODO don't forget to add load default action.
	final List<Action> actionList = new ArrayList<Action>();
	addActionIfNotNull(actionList, getConfigureAction());
	addActionIfNotNull(actionList, getSaveAction());
	addActionIfNotNull(actionList, getSaveAsAction());
	addActionIfNotNull(actionList, getSaveAsDefaultAction());
	addActionIfNotNull(actionList, getLoadDefaultAction());
	addActionIfNotNull(actionList, getRemoveAction());

	if(actionList.size() == 1){
	    return newButton(actionList.get(0));
	}
	if(actionList.size() > 1){
	    final List<ActionChanger<Void>> actionChangers = new ArrayList<ActionChanger<Void>>();
	    for(final Action action : actionList){
		addActionIfNotNull(actionChangers, actionChanger(action));
	    }
	    final ActionChangeButton button = new ActionChangeButton(actionChangers.get(0));
	    button.setState(ElementState.MEDIUM, true);
	    button.setFlat(false);
	    for (int actionIndex = 1; actionIndex < actionChangers.size(); actionIndex++) {
		button.addAction(actionChangers.get(actionIndex));
	    }
	    return button;
	}
	return null;
    }

    /**
     * Wraps the action in the {@link ActionChanger} class and returns it. Throws {@link NullPointerException} if the specified action is null.
     * 
     * @param action - specified action to be wrapped around {@link ActionChanger}, can not be null.
     * @return
     */
    private static ActionChanger<Void> actionChanger(final Action action) {
	return new ActionChanger<Void>(action.getValue(Action.NAME).toString()) {

	    private static final long serialVersionUID = 6527731925489863279L;

	    {
		putValue(SHORT_DESCRIPTION, action.getValue(SHORT_DESCRIPTION));
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		action.actionPerformed(null);
		super.postAction(value);
	    }
	};
    }

    /**
     * Adds the specified action to the list of actions if the action is not null.
     * 
     * @param actionList - specified list of actions to which specified action must be added.
     * @param action - specified action to be added.
     */
    private static <T extends Action> void addActionIfNotNull(final List<T> actionList, final T action) {
	if(actionList != null && action != null){
	    actionList.add(action);
	}
    }

    /**
     * Creates non-focusable {@link JButton} for passed action
     *
     * @param action
     * @return
     */
    private static JButton newButton(final Action action) {
	final JButton button = new JButton(action);
	button.setFocusable(false);
	return button;
    }

    /**
     * Creates property change listener for the currentAnalysisConfigurationView property. This listener selects new analysis set as the current one.
     * 
     * @return
     */
    private PropertyChangeListener createCurrentAnalysisChangeListener() {
	return new PropertyChangeListener() {

	    @SuppressWarnings("rawtypes")
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if("currentAnalysisConfigurationView".equals(evt.getPropertyName())){
		    ((AbstractAnalysisConfigurationView)evt.getNewValue()).select();
		}
	    }
	};
    }
}
