package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.domaintree.ITooltipProvider;
import ua.com.fielden.platform.swing.treewitheditors.CheckBoxTreeComponent;
import ua.com.fielden.platform.swing.treewitheditors.FilterMultipleCheckboxTreeCellRenderer;
import ua.com.fielden.platform.swing.treewitheditors.ITreeCheckingModelComponent;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;

/**
 * A tree for dynamic entity query criteria with totals.
 *
 * @author TG Team
 *
 */
public class CriteriaTree extends AbstractTree {
    private static final long serialVersionUID = 2969883425331891434L;

    public CriteriaTree(final CriteriaTreeModel criteriaTreeModel) {
	super(criteriaTreeModel);
	final List<ITooltipProvider> toolTipProviders = new ArrayList<ITooltipProvider>();
	toolTipProviders.add(createCriteriaCheckBoxToolTipProvider("selection criteria"));
	toolTipProviders.add(createResultantCheckBoxToolTipProvider("result set"));
	final CriteriaTreeCellRenderer renderer = new CriteriaTreeCellRenderer(new FilterMultipleCheckboxTreeCellRenderer(this, this.getModel(), createLabelToolTipProvider(), toolTipProviders) {
	    private static final long serialVersionUID = 5620649799595619625L;

	    @Override
	    protected ITreeCheckingModelComponent getCheckingComponent(final TreeCheckingModel treeCheckingModel) {
		final int modelIndex = CriteriaTree.this.getCheckingModelIndex(treeCheckingModel);
		return new CheckBoxTreeComponent(CriteriaTree.this, modelIndex) {
		    @Override
		    public void actionPerformed(final TreePath treePath) {
		    }
		};
	    }
	}, createAggregationFunctionToolTipProvider(), criteriaTreeModel);
	final CriteriaTreeCellEditor editor = new CriteriaTreeCellEditor(this, renderer, createAggregationFunctionToolTipProvider(), criteriaTreeModel);
	setCellRenderer(renderer);
	setEditable(true);
	setCellEditor(editor);
    }

    public CriteriaTreeModel getCriteriaTreeModel() {
	return (CriteriaTreeModel)getEntitiesTreeModel();
    }

    @Override
    public boolean isSelectsByChecking() {
	return true;
    }

    protected ITooltipProvider createAggregationFunctionToolTipProvider() {
	return new ITooltipProvider() {
	    @Override
	    public String getToolTip(final TreeNode treeNode) {
		return "<html>Click here to set aggregation function for <b>" + extractToolTipText(treeNode, false) + "</b> property</html>";
	    }
	};
    }
}
