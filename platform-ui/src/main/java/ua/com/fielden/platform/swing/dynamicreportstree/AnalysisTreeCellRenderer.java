package ua.com.fielden.platform.swing.dynamicreportstree;

import java.util.Date;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.domaintree.ITooltipProvider;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.swing.menu.filter.AbstractFilterableTreeModel;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTree;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTreeCellRenderer;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTreeCellRendererWithParameter;
import ua.com.fielden.platform.treemodel.AnalysisTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

public class AnalysisTreeCellRenderer extends MultipleCheckboxTreeCellRendererWithParameter {
    private static final long serialVersionUID = -1815282873942753876L;

    private final AnalysisTreeModel model;

    public AnalysisTreeCellRenderer(final MultipleCheckboxTreeCellRenderer renderer, final ITooltipProvider labelTooltipProvider, final AnalysisTreeModel analysisTreeModel) {
	super(renderer, labelTooltipProvider);
	this.model = analysisTreeModel;
    }

    @Override
    protected void setRendererHeight(final double rendererHeight) {
	super.setRendererHeight(rendererHeight);
    }

    @Override
    protected void configureParameterLabel(final MultipleCheckboxTree multipleCheckboxTree, final DefaultMutableTreeNode value) {
	// Retrieving EntitiesTreeModel from the tree if it's possible. If the it is not possible then entitiesTreeMdoel will be null.
	final TreeModel treeModel = multipleCheckboxTree.getModel();
	EntitiesTreeModel entitiesModel = null;
	if (treeModel instanceof AbstractFilterableTreeModel && ((AbstractFilterableTreeModel) treeModel).getOriginModel() instanceof EntitiesTreeModel) {
	    entitiesModel = (EntitiesTreeModel) ((AbstractFilterableTreeModel) treeModel).getOriginModel();
	}

	final String propertyName = entitiesModel.getPropertyNameFor(value);
	boolean labelVisibilty = false;
	String labelText = "";

	final Class<?> nodeType = ((TitledObject) (value).getUserObject()).getType();
	if (nodeType != null && Date.class.isAssignableFrom(nodeType)) {
	    labelText = model.getDistributionParameterFor(propertyName).toString();
	}
	final List<AnalysisPropertyAggregationFunction> functions = model.getAggregationParameterFor(propertyName);
	if (functions != null) {
	    for (final AnalysisPropertyAggregationFunction function : functions) {
		labelText += labelText.isEmpty() ? "" : ",";
		labelText += function.toString();
	    }
	}

	if (multipleCheckboxTree.getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).isPathChecked(new TreePath(value.getPath()))) {
	    labelVisibilty = true;
	}
	if (multipleCheckboxTree.getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(new TreePath(value.getPath())) && functions != null) {
	    labelVisibilty = true;
	}

	getLabel().setVisible(labelVisibilty);
	getLabel().setText(labelText);
    }
}
