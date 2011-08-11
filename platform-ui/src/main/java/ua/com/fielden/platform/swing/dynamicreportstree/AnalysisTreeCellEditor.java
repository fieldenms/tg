package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.HqlDateFunctions;
import ua.com.fielden.platform.swing.components.smart.autocompleter.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.TwoPropertyListCellRenderer;
import ua.com.fielden.platform.swing.components.textfield.UpperCaseTextField;
import ua.com.fielden.platform.swing.treewitheditors.EntitiesTree;
import ua.com.fielden.platform.swing.treewitheditors.ITooltipProvider;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTreeParameterCellEditor;
import ua.com.fielden.platform.treemodel.AnalysisTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

public class AnalysisTreeCellEditor extends MultipleCheckboxTreeParameterCellEditor {
    private static final long serialVersionUID = 4239011036384304748L;

    private final JComboBox valueEditor = new JComboBox();
    private final AutocompleterTextFieldLayer<AnalysisPropertyAggregationFunction> autocompleter;
    private final EnumValueMatcher<AnalysisPropertyAggregationFunction> valueMatcher = new EnumValueMatcher<AnalysisPropertyAggregationFunction>(AnalysisPropertyAggregationFunction.class);
    private final AnalysisTreeModel model;

    public AnalysisTreeCellEditor(final EntitiesTree tree, final AnalysisTreeCellRenderer renderer, final ITooltipProvider editorToolTipProvider, final AnalysisTreeModel analysisTreeModel) {
	super(tree, renderer, editorToolTipProvider);
	this.model = analysisTreeModel;

	// autocompleter:
	final TwoPropertyListCellRenderer<AnalysisPropertyAggregationFunction> cellRenderer = new TwoPropertyListCellRenderer<AnalysisPropertyAggregationFunction>("name()", "toString()");
	this.autocompleter = new AutocompleterTextFieldLayer<AnalysisPropertyAggregationFunction>(new UpperCaseTextField(), valueMatcher, AnalysisPropertyAggregationFunction.class, "name()", cellRenderer, "filter by aggregation functions...", ",");
	cellRenderer.setAuto(autocompleter.getAutocompleter());

	valueEditor.addPopupMenuListener(new PopupMenuListener() {
	    @Override
	    public void popupMenuCanceled(final PopupMenuEvent e) {
	    }

	    @Override
	    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
	    }

	    @Override
	    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		fireEditingStopped();
	    }
	});

	tree.getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).addTreeCheckingListener(new TreeCheckingListener() {
	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
		autocompleter.setVisible(e.isCheckedPath() && valueMatcher.getValuesSize() > 0);
	    }
	});
	tree.getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).addTreeCheckingListener(new TreeCheckingListener() {
	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
		valueEditor.setVisible(e.isCheckedPath() && Date.class.isAssignableFrom(getTitledObjectForPath(e.getPath()).getType()) && valueEditor.getModel().getSize() > 0);
	    }
	});

	renderer.setRendererHeight(getMinHeight());

	final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	final String EDITING_MODE = "Start or stop editing selected path";
	final Action editingStopAction = createEditingModeAction();
	valueEditor.getActionMap().put(EDITING_MODE, editingStopAction);
	valueEditor.getInputMap(JComponent.WHEN_FOCUSED).put(enter, EDITING_MODE);
	autocompleter.getActionMap().put(EDITING_MODE, editingStopAction);
	autocompleter.getInputMap(JComponent.WHEN_FOCUSED).put(enter, EDITING_MODE);
    }

    private Action createEditingModeAction() {
	return new AbstractAction() {
	    private static final long serialVersionUID = 4026043915769780852L;
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		fireEditingStopped();
	    }
	};
    }

    @Override
    protected JComponent getParameterEditingComponent(final DefaultMutableTreeNode value) {
	final TitledObject titledObject = getTitledObjectForPath(new TreePath(value.getPath()));
	if (titledObject != null) {
	    final String propertyName = getActualModel().getPropertyNameFor(value);
	    if (checkEditorType(titledObject)) {
		final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(HqlDateFunctions.values());
		if (getActualModel() != null) {
		    final HqlDateFunctions selectedItem = model.getDistributionParameterFor(propertyName);
		    if (selectedItem != null) {
			comboBoxModel.setSelectedItem(selectedItem);
		    }
		}
		valueEditor.setModel(comboBoxModel);
		if (comboBoxModel.getSize() > 0
			&& getCheckingTree().getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).isPathChecked(new TreePath((value).getPath()))) {
		    valueEditor.setVisible(true);
		} else {
		    valueEditor.setVisible(false);
		}
		valueEditor.setPreferredSize(new Dimension(100, valueEditor.getPreferredSize().height));
		return valueEditor;
	    }
	    Vector<AnalysisPropertyAggregationFunction> functions = model.getAvailableParametersFor(propertyName);

	    if (functions == null) {
		functions = new Vector<AnalysisPropertyAggregationFunction>();
	    }
	    valueMatcher.setValuesToSearchFor(functions);
	    if (getActualModel() != null) {
		autocompleter.getAutocompleter().getTextComponent().setText("");
		final List<AnalysisPropertyAggregationFunction> selectedItem = model.getAggregationParameterFor(propertyName);
		if (selectedItem != null && selectedItem.size() > 0) {
		    final Object selectedString = autocompleter.getAutocompleter().getSelectedHint(selectedItem, 0, 0, 0);
		    autocompleter.getAutocompleter().acceptHint(selectedString);
		}
	    }
	    if (functions.size() > 0 && getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(new TreePath((value).getPath()))) {
		autocompleter.setVisible(true);
	    } else {
		autocompleter.setVisible(false);
	    }
	    autocompleter.setPreferredSize(new Dimension(200, autocompleter.getPreferredSize().height));
	    return autocompleter;
	}
	return null;
    }

    /**
     * Returns true if the editor should be {@link JComboBox} otherwise returns false.
     * 
     * @param object
     * @return
     */
    private boolean checkEditorType(final TitledObject object) {
	return Date.class.isAssignableFrom(object.getType());
    }

    @Override
    protected void setParameterValueFor(final TreePath path) {
	final Object treeNode = path.getLastPathComponent();
	if (treeNode instanceof DefaultMutableTreeNode && getActualModel() != null) {
	    try {
		final List<AnalysisPropertyAggregationFunction> valuesToSet = new ArrayList<AnalysisPropertyAggregationFunction>();
		for (final AnalysisPropertyAggregationFunction function : autocompleter.values()) {
		    if (!valuesToSet.contains(function)) {
			valuesToSet.add(function);
		    }
		}
		if (valuesToSet.size() != 0) {
		    model.setAggregationParameterFor(getActualModel().getPropertyNameFor((DefaultMutableTreeNode) treeNode), valuesToSet);
		} else {
		    getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).removeCheckingPath(path);
		}
		model.setDistributionParameterFor(getActualModel().getPropertyNameFor((DefaultMutableTreeNode) treeNode), (HqlDateFunctions) valueEditor.getSelectedItem());
	    } catch (final IllegalArgumentException e) {
	    }
	}
    }

    @Override
    protected boolean canEditPath(final TreePath path) {
	final String propertyName = getActualModel().getPropertyNameFor((DefaultMutableTreeNode) path.getLastPathComponent());
	final TitledObject titledObject = getTitledObjectForPath(path);
	final Vector<AnalysisPropertyAggregationFunction> functions = titledObject != null ? model.getAvailableParametersFor(propertyName) : null;
	final boolean isTitledObject = titledObject != null;
	final boolean isPropertyFunction = isTitledObject && functions != null && functions.size() > 0;
	final boolean isDateFunction = isTitledObject && Date.class.isAssignableFrom(titledObject.getType()) && HqlDateFunctions.values().length > 0;
	final boolean isResultantSelected = getCheckingTree().getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(path);
	final boolean isCriteriaSelected = getCheckingTree().getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).isPathChecked(path);
	return (isPropertyFunction && isResultantSelected) || (isDateFunction && isCriteriaSelected);
    }

    @Override
    protected double getMinHeight() {
	final double valueEditorHeight = valueEditor.getPreferredSize().getHeight();
	final double autocompleterHeight = autocompleter.getPreferredSize().getHeight();
	return valueEditorHeight > autocompleterHeight ? valueEditorHeight : autocompleterHeight;
    }

}
