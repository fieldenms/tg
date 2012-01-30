package ua.com.fielden.platform.swing.review;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizardModel;
import ua.com.fielden.platform.swing.review.wizard.LocatorWizardModel;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.grid.TableModelWrapperUtils;

public class DynamicEntityLocator<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends DynamicEntityReview<T, DAO, R> {

    private static final long serialVersionUID = -1486241935748873213L;

    public DynamicEntityLocator(//
	    final DynamicEntityLocatorModel<T, DAO, R> model,//
	    final boolean showRecords,//
	    final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	super(model, showRecords, true, modelBuilder);
	final int selectionMode = model.isMultiselection() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION;
	getEntityGridInspector().setSelectionMode(selectionMode);
	final ListSelectionListener listener = model.createEgiSelectionListener();
	getEntityGridInspector().getSelectionModel().addListSelectionListener(listener);
	getEntityGridInspector().getColumnModel().getSelectionModel().addListSelectionListener(listener);
	getEntityGridInspector().addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		if (e.getClickCount() == 2) {
		    final int row = getEntityGridInspector().rowAtPoint(e.getPoint());
		    if (row >= 0) {
			model.getSelectAction().actionPerformed(null);
		    }
		}
	    }
	});
    }

    protected JPanel createActionButtonPanel(final DynamicEntityLocatorModel<T, DAO, R> model) {
	final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[fill][fill]push[][]", "[fill,grow][fill,grow]"));
	//	final JPanel controlPanel = new JPanel(new MigLayout("fill, insets 0", "[][]", "[][]"));
	final Pair<String, String> keyTitle = TitlesDescsGetter.getTitleAndDesc("key", model.getEntityType());
	final Pair<String, String> descTitle = TitlesDescsGetter.getTitleAndDesc("desc", model.getEntityType());
	final JCheckBox searchByDesc = new JCheckBox("Search by " + (descTitle.getKey() != null ? descTitle.getKey() : "description"));
	final JCheckBox searchByKeyAndDesc = new JCheckBox("Search by " + (keyTitle.getKey() != null ? keyTitle.getKey() : "key") + " and "
		+ (descTitle.getKey() != null ? descTitle.getKey() : "description"));
	if (model.isSearchByKey() && model.isSearchByDesc()) {
	    searchByKeyAndDesc.setSelected(true);
	} else if (model.isSearchByDesc()) {
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
		model.setUseForAutocompleter(useForAutocompleter.isSelected());
	    }
	});
	useForAutocompleter.setSelected(model.isUseForAutocompleter());

	buttonPanel.add(useForAutocompleter);
	buttonPanel.add(searchByDesc);
	buttonPanel.add(new JButton(model.getSelectAction()));
	buttonPanel.add(new JButton(model.getCancelAction()), "wrap");
	buttonPanel.add(searchByKeyAndDesc, "skip 1");

	return buttonPanel;
    }

    private void updateModel(final boolean searchByDesc, final boolean searchByKeyAndDesc) {
	if (searchByDesc) {
	    getEntityReviewModel().setSearchByDesc(true);
	    getEntityReviewModel().setSearchByKey(false);
	} else if (searchByKeyAndDesc) {
	    getEntityReviewModel().setSearchByKey(true);
	    getEntityReviewModel().setSearchByDesc(true);
	} else {
	    getEntityReviewModel().setSearchByDesc(false);
	    getEntityReviewModel().setSearchByKey(true);
	}
    }

    @Override
    protected void layoutComponents() {
	final DynamicEntityLocatorModel<T, DAO, R> locatorModel = getEntityReviewModel();
	final JPanel actionButtonPanel = createActionButtonPanel(locatorModel);
	String rowConstraints = "[fill][:400:, fill, grow]";
	final List<ComponentLayoutProperty> components = new ArrayList<ComponentLayoutProperty>();
	if (getActionPanel() != null && getActionPanel().getComponentCount() > 0) {
	    rowConstraints = "[fill]" + rowConstraints;
	    components.add(new ComponentLayoutProperty(getActionPanel(), "wrap"));
	}
	if (getCriteriaPanel() != null) {
	    components.add(new ComponentLayoutProperty(getCriteriaPanel(), "wrap"));
	    rowConstraints = "[fill]" + rowConstraints;
	}
	components.add(new ComponentLayoutProperty(getButtonPanel(), "wrap"));
	components.add(new ComponentLayoutProperty(getProgressLayer(), "wrap"));
	if (actionButtonPanel != null) {
	    components.add(new ComponentLayoutProperty(actionButtonPanel, ""));
	    rowConstraints += "[fill]";
	}

	setLayout(new MigLayout("fill, insets 10", "[:400:, fill, grow]", rowConstraints));
	for (int componentIndex = 0; componentIndex < components.size(); componentIndex++) {
	    add(components.get(componentIndex).getComponent(), components.get(componentIndex).getComponentConstraint());
	}
    }

    protected List<T> getSelectedItems() {
	final EntityGridInspector<T> itemTable = getEntityGridInspector();
	final int[] actualRows = TableModelWrapperUtils.getActualRowsAt(itemTable.getModel(), itemTable.getSelectedRows(), false);
	final List<T> selectedItems = new ArrayList<T>();
	for (final int row : actualRows) {
	    final T selectedItem = itemTable.getActualModel().instance(row);
	    if (selectedItem != null) {
		selectedItems.add(selectedItem);
	    }
	}
	return selectedItems;
    }

    private static class ComponentLayoutProperty {
	private final JComponent component;
	private final String componentConstraint;

	public ComponentLayoutProperty(final JComponent component, final String componentConstraint) {
	    this.component = component;
	    this.componentConstraint = componentConstraint;
	}

	public JComponent getComponent() {
	    return component;
	}

	public String getComponentConstraint() {
	    return componentConstraint;
	}

    }

    @Override
    public DynamicEntityLocatorModel<T, DAO, R> getEntityReviewModel() {
	return (DynamicEntityLocatorModel<T, DAO, R>) super.getEntityReviewModel();
    }

    @Override
    public void saveValues() {
	super.saveValues();
	final AbstractWizardModel<T, DAO, R> wizardModel = getModelBuilder().getWizardModel();
	if (wizardModel instanceof LocatorWizardModel) {
	    ((LocatorWizardModel) wizardModel).setUseForAutocompleter(getEntityReviewModel().isUseForAutocompleter());
	    ((LocatorWizardModel) wizardModel).setSearchByDesc(getEntityReviewModel().isSearchByDesc());
	    ((LocatorWizardModel) wizardModel).setSearchByKey(getEntityReviewModel().isSearchByKey());
	}
    }

    @Override
    protected JComponent buildActionChanger() {
	final List<String> actionOrder = new ArrayList<String>();
	actionOrder.add("Configure");
	actionOrder.add("Save");
	return getEntityReviewModel().getActionChangerBuilder().buildActionChanger(actionOrder);
    }

}
