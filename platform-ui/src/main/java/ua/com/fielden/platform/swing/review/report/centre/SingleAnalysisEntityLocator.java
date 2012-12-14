package ua.com.fielden.platform.swing.review.report.centre;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent.LocatorAction;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.grid.TableModelWrapperUtils;

public class SingleAnalysisEntityLocator<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ILocatorDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = 8426409155798286535L;

    private final JPanel locatorPanel;
    private final Action closeAction;
    private final Action selectAction;
    private final List<T> selectedEntities = new ArrayList<T>();

    public SingleAnalysisEntityLocator(final EntityLocatorModel<T> model, final LocatorConfigurationView<T, ?> owner) {
	super(model, owner);
	this.closeAction = createCloseAction();
	this.selectAction = createSelectAction();
	this.locatorPanel = createLocatorPanel();
	addLoadListener(new ILoadListener() {

	    @Override
	    public void viewWasLoaded(final LoadEvent event) {
		final EntityGridInspector<T> egi = getSingleAnalysis().getPreviousView().getEgiPanel().getEgi();
		final ListSelectionListener listener = createEgiSelectionListener(egi, getOwner().isMultipleSelection());
		egi.setSelectionMode(getOwner().isMultipleSelection() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		egi.getSelectionModel().addListSelectionListener(listener);
		egi.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		egi.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {
			    final int row = egi.rowAtPoint(e.getPoint());
			    if (row >= 0) {
				selectAction.actionPerformed(null);
			    }
			}
		    }
		});
		removeLoadListener(this);
	    }
	});

	layoutComponents();
    }

    @SuppressWarnings("unchecked")
    @Override
    public GridConfigurationView<T, ILocatorDomainTreeManagerAndEnhancer> getSingleAnalysis() {
        return (GridConfigurationView<T, ILocatorDomainTreeManagerAndEnhancer>)super.getSingleAnalysis();
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
	return Collections.unmodifiableList(selectedEntities);
    }

    public void addLocatorEventListener(final ILocatorEventListener l){
	listenerList.add(ILocatorEventListener.class, l);
    }

    public void removeLocatorEventListener(final ILocatorEventListener l){
	listenerList.remove(ILocatorEventListener.class, l);
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
		return null;
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
	final GridConfigurationModel<T, ILocatorDomainTreeManagerAndEnhancer> configModel = GridConfigurationModel.createWithDefaultQueryCustomiser(getModel().getCriteria());
	return GridConfigurationView.createMainDetailsWithDefaultCustomiser(configModel, getOwner().getDetailsCache(), null, this, getReviewProgressLayer());
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

    private ListSelectionListener createEgiSelectionListener(final EntityGridInspector<T> egi, final boolean isMultipleSelection) {
	return new ListSelectionListener() {

	    @Override
	    public void valueChanged(final ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    if (e.getFirstIndex() < 0 || e.getLastIndex() < 0) {
			return;
		    }
		    final int rows[] = isMultipleSelection ? new int[e.getLastIndex() - e.getFirstIndex() + 1] //
			    : new int[e.getFirstIndex() == e.getLastIndex() ? 1 : 2];
		    if (!isMultipleSelection) {
			if (e.getFirstIndex() == e.getLastIndex()) {
			    rows[0] = e.getFirstIndex();
			} else {
			    rows[0] = e.getFirstIndex();
			    rows[1] = e.getLastIndex();
			}
		    } else {
			for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
			    rows[rowIndex] = e.getFirstIndex() + rowIndex;
			}
		    }
		    final int actualRows[] = TableModelWrapperUtils.getActualRowsAt(egi.getModel(), rows, false);
		    for (int rowIndex = 0; rowIndex < actualRows.length; rowIndex++) {
			final T instance = egi.getActualModel().instance(actualRows[rowIndex]);
			final boolean isSelected = egi.isRowSelected(rows[rowIndex]);
			if (isSelected) {
			    performSelection(instance);
			} else {
			    performDeselect(instance);
			}
		    }
		    //TODO Please consider whether select or close actions should be configured according to the selected entities list.
		    //if (selectedEntities.isEmpty()) {
		    //	  selectAction.setEnabled(false);
		    //} else {
		    //	  selectAction.setEnabled(true);
		    //}
		}
	    }

	    public boolean isSelected(final T entityToCheck) {
		return selectedEntities.contains(entityToCheck);
	    }

	    public void performDeselect(final T selectedObject) {
		selectedEntities.remove(selectedObject);
	    }

	    public void performSelection(final T selectedObject) {
		if (isMultipleSelection) {
		    if (!isSelected(selectedObject)) {
			selectedEntities.add(selectedObject);
		    }
		} else {
		    selectedEntities.clear();
		    selectedEntities.add(selectedObject);
		}
	    }

	};
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

	final Class<?> managedType = getModel().getCriteria().getManagedType();
	final ILocatorDomainTreeManager ldtm = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();

	final boolean containDesc = AnnotationReflector.isAnnotationPresent(DescTitle.class, managedType);

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
		selectedEntities.clear();
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
