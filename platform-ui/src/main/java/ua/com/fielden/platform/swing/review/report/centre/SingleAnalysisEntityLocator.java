package ua.com.fielden.platform.swing.review.report.centre;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisViewForLocator;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModelForLocator;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationViewForLocator;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent.LocatorAction;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class SingleAnalysisEntityLocator<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ILocatorDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = 8426409155798286535L;

    private final JPanel locatorPanel;
    private final Action closeAction;
    private final Action selectAction;

    public SingleAnalysisEntityLocator(final EntityLocatorModel<T> model, final LocatorConfigurationView<T, ?> owner) {
	super(model, owner);
	this.closeAction = createCloseAction();
	this.selectAction = createSelectAction();
	this.locatorPanel = createLocatorPanel();
	layoutComponents();
    }

    @SuppressWarnings("unchecked")
    @Override
    public GridConfigurationView<T, ILocatorDomainTreeManagerAndEnhancer> getSingleAnalysis() {
        return (GridConfigurationView<T, ILocatorDomainTreeManagerAndEnhancer>)super.getSingleAnalysis();
    }

    public GridAnalysisViewForLocator<T> getAnalysisView(){
	return (GridAnalysisViewForLocator<T>)getSingleAnalysis().getPreviousView();
    }

    @Override
    public EntityLocatorModel<T> getModel() {
	return (EntityLocatorModel<T>)super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocatorConfigurationView<T, ?> getOwner() {
	return (LocatorConfigurationView<T, ?>)super.getOwner();
    }

    public List<T> getSelectedEntities() {
	return getAnalysisView().getEnhancedSelectedEntities();
    }

    public void addLocatorEventListener(final ILocatorEventListener l){
	listenerList.add(ILocatorEventListener.class, l);
    }

    public void removeLocatorEventListener(final ILocatorEventListener l){
	listenerList.remove(ILocatorEventListener.class, l);
    }

    public Action getSelectAction() {
	return selectAction;
    }

    public Action getCloseAction() {
	return closeAction;
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return new ConfigureAction(getOwner()) {

	    private static final long serialVersionUID = 1777882711643795897L;

	    {
		putValue(Action.NAME, "Configure");
		putValue(Action.SHORT_DESCRIPTION, "Configure this entity locator");
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		getOwner().getModel().freeze();
		return super.action(e);
	    }

	    @Override
	    protected void restoreAfterError() {
		if(getOwner().getModel().isInFreezedPhase()){
		    getOwner().getModel().discard();
		}
	    }
	};
    }

    @Override
    protected List<Action> createCustomActionList() {
	final List<Action> customActions = new ArrayList<Action>();
	customActions.add(getConfigureAction());
	customActions.add(getOwner().getSave());
	customActions.add(getOwner().getSaveAsDefault());
	customActions.add(getOwner().getLoadDefault());
	return customActions;
    }

    /**
     * Creates single grid analysis view.
     *
     * @return
     */
    @Override
    protected GridConfigurationView<T, ILocatorDomainTreeManagerAndEnhancer> createDefaultAnalysis(){
	final GridConfigurationModelForLocator<T> configModel = new GridConfigurationModelForLocator<>(getModel().getCriteria());
	return new GridConfigurationViewForLocator<>(configModel, this, getReviewProgressLayer());
    }

    @Override
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
	//Adds the entity locator specific actions.
	rowConstraints.append(addToComponents(components, "[fill]", getLocatorPanel()));

	removeAll();
	setLayout(new MigLayout("fill, insets 5", "[:400:, fill, grow]", isEmpty(rowConstraints.toString()) ? "[fill, grow]" : rowConstraints.toString()));

	for(int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++){
	    add(components.get(componentIndex), "wrap");
	}
	add(components.get(components.size()-1));
    }

    private JPanel getLocatorPanel() {
	return locatorPanel;
    }

    private void fireLocatorEvent(final LocatorEvent event){
	for(final ILocatorEventListener listener : listenerList.getListeners(ILocatorEventListener.class)){
	    listener.locatorActionPerformed(event);
	}
    }

    /**
     * Creates locator specific panel with next controls: "use for autocompleter" check box, "filter by description" and "filter by key or description" , close and select buttons.
     *
     * @return
     */
    private JPanel createLocatorPanel() {

	final Class<T> managedType = getModel().getCriteria().getManagedType();
	final ILocatorDomainTreeManager ldtm = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();

	final boolean containDesc = EntityUtils.hasDescProperty(managedType);

	final JPanel locatorPanel = new JPanel(new MigLayout("fill, insets 0", "[fill]" + (containDesc ? "[fill]" : "") + "push[][]", "[fill,grow]" + (containDesc ? "[fill,grow]" : "")));


	final Pair<JCheckBox, JCheckBox> filterCheckBoxes = containDesc ? createFilterCheckBoxes(ldtm, managedType) : null;

	locatorPanel.add(createUseForAutocompleterCheckBox(ldtm));
	if(filterCheckBoxes != null){
	    locatorPanel.add(filterCheckBoxes.getKey());
	}
	locatorPanel.add(new JButton(selectAction));
	locatorPanel.add(new JButton(closeAction), (filterCheckBoxes != null ? "wrap" : ""));
	if(filterCheckBoxes != null){
	    locatorPanel.add(filterCheckBoxes.getValue(), "skip 1");
	}
	return locatorPanel;
    }

    /**
     * Creates the "use for autocompleter" check box.
     * @param ldtm
     *
     * @param descTitle
     * @return
     */
    private JCheckBox createUseForAutocompleterCheckBox(final ILocatorDomainTreeManager ldtm){
	final JCheckBox useForAutocompleter = new JCheckBox("Use for autocompleter");
	useForAutocompleter.setSelected(ldtm.isUseForAutocompletion());
	useForAutocompleter.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		ldtm.setUseForAutocompletion(useForAutocompleter.isSelected());
	    }
	});
	return useForAutocompleter;
    }

    /**
     * Creates pair of checkbox : first - is "filter by description", and the second one - "filter by key or description".
     *
     * @param ldtm
     * @param managedType
     * @return
     */
    private Pair<JCheckBox, JCheckBox> createFilterCheckBoxes(final ILocatorDomainTreeManager ldtm, final Class<?> managedType){
	final Pair<String, String> keyTitle = TitlesDescsGetter.getTitleAndDesc("key", managedType);
	final Pair<String, String> descTitle = TitlesDescsGetter.getTitleAndDesc("desc", managedType);
	final JCheckBox searchByDesc = new JCheckBox("Search by " + (!StringUtils.isEmpty(descTitle.getKey()) ? descTitle.getKey() : "description"));
	final JCheckBox searchByKeyAndDesc = new JCheckBox("Search by " + (!StringUtils.isEmpty(keyTitle.getKey()) ? keyTitle.getKey() : "key") + " and "
		+ (!StringUtils.isEmpty(descTitle.getKey()) ? descTitle.getKey() : "description"));
	if (SearchBy.DESC_AND_KEY == ldtm.getSearchBy()) {
	    searchByKeyAndDesc.setSelected(true);
	} else if (SearchBy.DESC == ldtm.getSearchBy()) {
	    searchByDesc.setSelected(true);
	}
	searchByDesc.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (searchByDesc.isSelected()) {
		    searchByKeyAndDesc.setSelected(false);
		}
		updateModel(searchByDesc.isSelected(), searchByKeyAndDesc.isSelected());
	    }

	});
	searchByKeyAndDesc.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (searchByKeyAndDesc.isSelected()) {
		    searchByDesc.setSelected(false);
		}
		updateModel(searchByDesc.isSelected(), searchByKeyAndDesc.isSelected());
	    }
	});
	return new Pair<JCheckBox, JCheckBox>(searchByDesc, searchByKeyAndDesc);
    }

    private Action createCloseAction() {
	return new AbstractAction("Close") {

	    private static final long serialVersionUID = -6132563247962017273L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getAnalysisView().resetLocatorSelection();
		fireLocatorEvent(new LocatorEvent(SingleAnalysisEntityLocator.this, LocatorAction.CLOSE));
	    }
	};
    }

    private Action createSelectAction() {
	return new AbstractAction("Select") {

	    private static final long serialVersionUID = -6382883149032213608L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		fireLocatorEvent(new LocatorEvent(SingleAnalysisEntityLocator.this, LocatorAction.SELECT));
	    }
	};
    }

    private void updateModel(final boolean searchByDesc, final boolean searchByKeyAndDesc) {
	final ILocatorDomainTreeManager ldtm = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();
	if (searchByDesc) {
	    ldtm.setSearchBy(SearchBy.DESC);
	} else if (searchByKeyAndDesc) {
	    ldtm.setSearchBy(SearchBy.DESC_AND_KEY);
	} else {
	    ldtm.setSearchBy(SearchBy.KEY);
	}
    }
}
