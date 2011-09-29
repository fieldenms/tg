package ua.com.fielden.platform.swing.dynamicreportstree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.domaintree.ITooltipProvider;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.swing.menu.filter.AbstractFilterableTreeModel;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTree;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTreeCellRenderer;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTreeCellRendererWithParameter;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;

public class CriteriaTreeCellRenderer extends MultipleCheckboxTreeCellRendererWithParameter {

    private static final long serialVersionUID = -9057342278488448337L;

    private final CriteriaTreeModel model;

    public CriteriaTreeCellRenderer(final MultipleCheckboxTreeCellRenderer renderer, final ITooltipProvider aggregationFunctionTooltipProvider, final CriteriaTreeModel model) {
	super(renderer, aggregationFunctionTooltipProvider);
	this.model = model;
    }

    @Override
    protected void setRendererHeight(final double rendererHeight) {
	super.setRendererHeight(rendererHeight);
    }

    @Override
    protected void configureParameterLabel(final MultipleCheckboxTree tree, final DefaultMutableTreeNode value) {
	// Retrieving EntitiesTreeModel from the tree if it's possible. If the it is not possible then entitiesTreeMdoel will be null.
	final TreeModel treeModel = tree.getModel();
	EntitiesTreeModel entitiesModel = null;
	if (treeModel instanceof AbstractFilterableTreeModel && ((AbstractFilterableTreeModel) treeModel).getOriginModel() instanceof EntitiesTreeModel) {
	    entitiesModel = (EntitiesTreeModel) ((AbstractFilterableTreeModel) treeModel).getOriginModel();
	}
	// Retrieving PropertyAggregationFunction from the ITreeParameterManager.
	PropertyAggregationFunction function = null;
	if (entitiesModel != null) {
	    function = model.getTotalsParameterFor(entitiesModel.getPropertyNameFor(value));
	}

	// Configuring label visibility. if the path for EntitiesTreeColumn.TABLE_HEADER_COLUMN
	// checking model is checked then label will be visible otherwise it won't.
	if (tree.getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathChecked(new TreePath(value.getPath()))) {
	    getLabel().setVisible(true);
	} else {
	    getLabel().setVisible(false);
	}

	// Configuring text property of the label.
	if (function != null) {
	    getLabel().setText(function.toString());
	} else {
	    getLabel().setText("");
	}

    }

}
