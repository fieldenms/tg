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
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent.LocatorAction;
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

    public SingleAnalysisEntityLocator(final EntityLocatorModel<T> model, final LocatorConfigurationView<T, ?> owner, final boolean isMultipleSelection) {
	super(model, owner);
	createReview();
	this.closeAction = createCloseAction();
	this.selectAction = createSelectAction();
	this.locatorPanel = createLocatorPanel();
	final EntityGridInspector<T> egi = getEntityGridInspector(this);
	final ListSelectionListener listener = createEgiSelectionListener(egi, isMultipleSelection);
	egi.setSelectionMode(isMultipleSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
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
	layoutComponents();
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

    @Override
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

    private JPanel createLocatorPanel() {
	final JPanel locatorPanel = new JPanel(new MigLayout("fill, insets 0", "[fill][fill]push[][]", "[fill,grow][fill,grow]"));

	final Class<T> entityType = getModel().getCriteria().getEntityClass();
	final Class<?> managedType = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer().getEnhancer().getManagedType(entityType);
	final ILocatorDomainTreeManager ldtm = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();

	final Pair<String, String> keyTitle = TitlesDescsGetter.getTitleAndDesc("key", managedType);
	final Pair<String, String> descTitle = TitlesDescsGetter.getTitleAndDesc("desc", managedType);
	final JCheckBox searchByDesc = new JCheckBox("Search by " + (descTitle.getKey() != null ? descTitle.getKey() : "description"));
	final JCheckBox searchByKeyAndDesc = new JCheckBox("Search by " + (keyTitle.getKey() != null ? keyTitle.getKey() : "key") + " and "
		+ (descTitle.getKey() != null ? descTitle.getKey() : "description"));
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
	final JCheckBox useForAutocompleter = new JCheckBox("Use for autocompleter");
	useForAutocompleter.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		ldtm.setUseForAutocompletion(useForAutocompleter.isSelected());
	    }
	});
	useForAutocompleter.setSelected(ldtm.isUseForAutocompletion());

	locatorPanel.add(useForAutocompleter);
	locatorPanel.add(searchByDesc);
	locatorPanel.add(new JButton(selectAction));
	locatorPanel.add(new JButton(closeAction), "wrap");
	locatorPanel.add(searchByKeyAndDesc, "skip 1");
	return locatorPanel;
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
