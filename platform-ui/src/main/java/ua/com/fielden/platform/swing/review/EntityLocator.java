package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

import com.jidesoft.grid.TableModelWrapperUtils;

public class EntityLocator<T extends AbstractEntity, DAO extends IEntityDao<T>, C extends EntityQueryCriteria<T, DAO>> extends EntityReview<T, DAO, C> {

    private static final long serialVersionUID = -3885067128573250957L;

    public EntityLocator(final EntityLocatorModel<T, DAO, C> model) {
	this(model, false);
    }

    public EntityLocator(final EntityLocatorModel<T, DAO, C> model, final boolean showRecords) {
	super(model, showRecords);
	getEntityGridInspector().setSelectionMode(model.getSelectionListener().isMultiselection() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
		: ListSelectionModel.SINGLE_SELECTION);

    }

    protected JPanel createActionButtonPanel(final EntityLocatorModel<T, DAO, C> model) {
	final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0"));
	buttonPanel.add(new JButton(model.createSelectAction(this)), "dock east");
	return buttonPanel;
    }

    @Override
    protected void layoutComponents() {
	JPanel actionButtonPanel;
	if (getEntityReviewModel() instanceof EntityLocatorModel) {
	    final EntityLocatorModel<T, DAO, C> locatorModel = (EntityLocatorModel<T, DAO, C>) getEntityReviewModel();
	    actionButtonPanel = createActionButtonPanel(locatorModel);
	} else {
	    actionButtonPanel = null;
	}
	String rowConstraints = "[fill][:400:, fill, grow]";
	final List<ComponentLayoutProperty> components = new ArrayList<ComponentLayoutProperty>();
	if (getActionPanel() != null && getActionPanel().getComponentCount() > 0) {
	    rowConstraints += "[fill]";
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
    public String getInfo() {
	return "Facility to locate entities.";
    }
}
