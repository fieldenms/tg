package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.basic.autocompleter.EnumValueMatcher;
import ua.com.fielden.platform.entity.annotation.Collectional;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.swing.treewitheditors.EntitiesTree;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTreeParameterCellEditor;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
import ua.com.fielden.platform.treemodel.rules.ITooltipProvider;

/**
 * Cell editor for criteria tree.
 *
 * TODO (mainly 2 Oleh) : this cell editor should be modified to take into account
 * <p>
 *  a) collectional aggregation result-set parameters
 * <p>
 *  b) possibly collectional aggregation criteria parameters.
 * <p>
 *  At this stage collections is generated inside main class hierarchy in the end of properties list (used {@link Collectional} annotation).
 *  Some initial approach for a) case commented below.
 *
 * @author
 *
 */
public class CriteriaTreeCellEditor extends MultipleCheckboxTreeParameterCellEditor {
    private static final long serialVersionUID = 4094195515994144216L;
    private final JComboBox valueEditor = new JComboBox();
    private final CriteriaTreeModel model;

//    private final AutocompleterTextFieldLayer<AnalysisPropertyAggregationFunction> autocompleter;
    private final EnumValueMatcher<AnalysisPropertyAggregationFunction> valueMatcher = new EnumValueMatcher<AnalysisPropertyAggregationFunction>(AnalysisPropertyAggregationFunction.class);

    public CriteriaTreeCellEditor(final EntitiesTree tree, final CriteriaTreeCellRenderer renderer, final ITooltipProvider editorToolTipProvider, final CriteriaTreeModel model) {
	super(tree, renderer, editorToolTipProvider);
	this.model = model;

//	// autocompleter for collectional properties:
//	final TwoPropertyListCellRenderer<AnalysisPropertyAggregationFunction> cellRenderer = new TwoPropertyListCellRenderer<AnalysisPropertyAggregationFunction>("name()", "toString()");
//	this.autocompleter = new AutocompleterTextFieldLayer<AnalysisPropertyAggregationFunction>(new JTextField(), valueMatcher, AnalysisPropertyAggregationFunction.class, "name()", cellRenderer, "filter by aggregation functions...", ",");
//	cellRenderer.setAuto(autocompleter.getAutocompleter());

	valueEditor.addPopupMenuListener(new PopupMenuListener() {
	    @Override public void popupMenuCanceled(final PopupMenuEvent e) { }
	    @Override public void popupMenuWillBecomeVisible(final PopupMenuEvent e) { }
	    @Override
	    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		fireEditingStopped();
	    }
	});
	// listener for enabling/disabling autocompleter/valueEditor while clicking on second checkbox:
	tree.getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).addTreeCheckingListener(new TreeCheckingListener() {
	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
//		final TitledObject entityTitledObject = getEntityTitledObject(e.getPath());
//		// TODO check!!!
//		if (entityTitledObject != null) {
//		    if (model.isCollectionalPropertyType(entityTitledObject.getType())) {
//			autocompleter.setVisible(e.isCheckedPath() && valueMatcher.getValuesSize() > 0);
//		    } else {
			valueEditor.setVisible(e.isCheckedPath() && valueEditor.getModel().getSize() > 0);
//		    }
//		}
	    }
	});

	renderer.setRendererHeight(getMinHeight());

	final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	final String EDITING_MODE = "Start or stop editing selected path";
	final Action editingStopAction = createEditingModeAction();
	valueEditor.getActionMap().put(EDITING_MODE, editingStopAction);
	valueEditor.getInputMap(JComponent.WHEN_FOCUSED).put(enter, EDITING_MODE);
//	autocompleter.getActionMap().put(EDITING_MODE, editingStopAction);
//	autocompleter.getInputMap(JComponent.WHEN_FOCUSED).put(enter, EDITING_MODE);
    }

//    private TitledObject getEntityTitledObject(final TreePath path) {
//	if (path.getPathCount() > 1){
//	    final Object[] firstTwo = { path.getPathComponent(0), path.getPathComponent(1) };
//	    return getTitledObjectForPath(new TreePath(firstTwo));
//	} else {
//	    return getTitledObjectForPath(new TreePath(path.getPathComponent(0)));
//	}
//
//    }

    private Action createEditingModeAction() {
	return new AbstractAction() {
	    private static final long serialVersionUID = 4026043915769780852L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		fireEditingStopped();
	    }
	};
    }

    /**
     * Returns the component for editing {@code value} parameter.
     */
    @Override
    protected JComponent getParameterEditingComponent(final DefaultMutableTreeNode value) {
	final TitledObject titledObject = getTitledObjectForPath(new TreePath(value.getPath()));
	if (titledObject != null) {
//	    final Class<? extends AbstractEntity> entityType = (Class<? extends AbstractEntity>) getEntityTitledObject(new TreePath(value.getPath())).getType();
//	    final boolean isCollectional = model.isCollectionalPropertyType(entityType);
//	    if (!isCollectional) {
		Vector<PropertyAggregationFunction> functions = PropertyAggregationFunction.getFunctionForType(titledObject.getType());
		if (functions == null) {
		    functions = new Vector<PropertyAggregationFunction>();
		}
		final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(functions);
		if (getActualModel() != null) {
		    final PropertyAggregationFunction selectedItem = model.getTotalsParameterFor(getActualModel().getPropertyNameFor(value));
		    if (selectedItem != null) {
			comboBoxModel.setSelectedItem(selectedItem);
		    }
		}
		valueEditor.setModel(comboBoxModel);
		valueEditor.setVisible(functions.size() > 0
			&& getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(new TreePath((value).getPath())));
		return valueEditor;
//	    } else {
//		Vector<AnalysisPropertyAggregationFunction> functions = AnalysisPropertyAggregationFunction.getFunctionForType(titledObject.getType());
//		if (functions == null) {
//		    functions = new Vector<AnalysisPropertyAggregationFunction>();
//		}
//		valueMatcher.setValuesToSearchFor(functions);
//		if (getActualModel() != null) {
//		    autocompleter.getAutocompleter().getTextComponent().setText("");
//		    final List<AnalysisPropertyAggregationFunction> selectedItem = model.getCollectionAggregationParameterFor(entityType, getActualModel().getPropertyNameFor(value));
//		    if (selectedItem != null && selectedItem.size() > 0) {
//			final Object selectedString = autocompleter.getAutocompleter().getSelectedHint(selectedItem, 0, 0, 0);
//			autocompleter.getAutocompleter().acceptHint(selectedString);
//		    }
//		}
//		autocompleter.setVisible(functions.size() > 0
//			&& getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(new TreePath((value).getPath())));
//		autocompleter.setPreferredSize(new Dimension(200, autocompleter.getPreferredSize().height));
//		return autocompleter;
//	    }
	}
	return null;
    }

    /**
     * Set the selected value of the editing component for the edited path.
     *
     * @param path
     */
    @Override
    protected void setParameterValueFor(final TreePath path) {
	final Object treeNode = path.getLastPathComponent();
	if (treeNode instanceof DefaultMutableTreeNode && getActualModel() != null) {
	    try {
//		final Class<? extends AbstractEntity> entityType = (Class<? extends AbstractEntity>) getEntityTitledObject(path).getType();
//		final boolean isCollectional = model.isCollectionalPropertyType(entityType);
//		if (!isCollectional) {
		    model.setTotalsParameterFor(getActualModel().getPropertyNameFor((DefaultMutableTreeNode) treeNode), (PropertyAggregationFunction) valueEditor.getSelectedItem());
//		} else {
//		    final List<AnalysisPropertyAggregationFunction> valuesToSet = new ArrayList<AnalysisPropertyAggregationFunction>();
//		    for (final AnalysisPropertyAggregationFunction function : autocompleter.values()) {
//			if (!valuesToSet.contains(function)) {
//			    valuesToSet.add(function);
//			}
//		    }
//		    if (valuesToSet.size() != 0) {
//			model.setCollectionAggregationParameterFor(entityType, getActualModel().getPropertyNameFor((DefaultMutableTreeNode) treeNode), valuesToSet);
//		    } else {
//			getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).removeCheckingPath(path);
//		    }
//		}
	    } catch (final IllegalArgumentException e) {
	    }
	}
    }

    /**
     * Returns true if the path can be edited. This routine will be invoked when user clicked the tree node caption.
     *
     * @param path
     * @return
     */
    @Override
    protected boolean canEditPath(final TreePath path) {
	final TitledObject titledObject = getTitledObjectForPath(path);
	final boolean isTitledObject = titledObject != null;
	final boolean isPropertyFunction = isTitledObject && PropertyAggregationFunction.getFunctionForType(titledObject.getType()) != null;
	final boolean isResultantSelected = getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(path);
	return isPropertyFunction && isResultantSelected;

//	final TitledObject titledObject = getTitledObjectForPath(path);
//	final Vector<AnalysisPropertyAggregationFunction> functions = titledObject != null ? AnalysisPropertyAggregationFunction.getFunctionForType(titledObject.getType()) : null;
//	final boolean isTitledObject = titledObject != null;
//	final boolean isPropertyFunction = isTitledObject && functions != null && functions.size() > 0;
//	final boolean isDateFunction = isTitledObject && Date.class.isAssignableFrom(titledObject.getType()) && HqlDateFunctions.values().length > 0;
//	final boolean isResultantSelected = getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(path);
//	final boolean isCriteriaSelected = getCheckingTree().getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).isPathChecked(path);
//	return (isPropertyFunction && isResultantSelected) || (isDateFunction && isCriteriaSelected);
    }

    @Override
    protected double getMinHeight() {
    	return valueEditor.getPreferredSize().getHeight();
    }
//    @Override
//    protected double getMinHeight() {
//	final double valueEditorHeight = valueEditor.getPreferredSize().getHeight();
//	final double autocompleterHeight = autocompleter.getPreferredSize().getHeight();
//	return valueEditorHeight > autocompleterHeight ? valueEditorHeight : autocompleterHeight;
//    }
}