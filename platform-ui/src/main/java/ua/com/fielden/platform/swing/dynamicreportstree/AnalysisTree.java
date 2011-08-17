package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.treewitheditors.CheckBoxTreeComponent;
import ua.com.fielden.platform.swing.treewitheditors.FilterMultipleCheckboxTreeCellRenderer;
import ua.com.fielden.platform.swing.treewitheditors.ITreeCheckingModelComponent;
import ua.com.fielden.platform.treemodel.AnalysisTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
import ua.com.fielden.platform.treemodel.rules.ITooltipProvider;
import ua.com.fielden.platform.utils.EntityUtils;

public class AnalysisTree extends AbstractTree {
    private static final long serialVersionUID = 7355908219003968256L;

    public AnalysisTree(final AnalysisTreeModel treeModel) {
	super(treeModel, createFilterForDistributionProperties(), createFilterForAggregationProperties());
	final List<ITooltipProvider> toolTipProviders = new ArrayList<ITooltipProvider>();
	toolTipProviders.add(createCriteriaCheckBoxToolTipProvider("distribution properties"));
	toolTipProviders.add(createResultantCheckBoxToolTipProvider("aggregation properties"));
	final AnalysisTreeCellRenderer renderer = new AnalysisTreeCellRenderer(new FilterMultipleCheckboxTreeCellRenderer(this, this.getModel(), createLabelToolTipProvider(), toolTipProviders) {
	    private static final long serialVersionUID = 5620649799595619625L;

	    @Override
	    protected ITreeCheckingModelComponent getCheckingComponent(final TreeCheckingModel treeCheckingModel) {
		final int modelIndex = AnalysisTree.this.getCheckingModelIndex(treeCheckingModel);
		return new CheckBoxTreeComponent(AnalysisTree.this, modelIndex) {
		    @Override
		    public void actionPerformed(final TreePath treePath) {
		    }
		};
	    }
	}, createAggregationFunctionToolTipProvider(), treeModel);
	final AnalysisTreeCellEditor editor = new AnalysisTreeCellEditor(this, renderer, createAggregationFunctionToolTipProvider(), treeModel);
	setCellRenderer(renderer);
	setEditable(true);
	setCellEditor(editor);
    }

    /**
     * Model for analysis tree.
     *
     * @return
     */
    public AnalysisTreeModel getAnalysisTreeModel() {
	return (AnalysisTreeModel) getEntitiesTreeModel();
    }

    @Override
    public boolean isSelectsByChecking() {
	return true;
    }

    protected ITooltipProvider createAggregationFunctionToolTipProvider() {
	return new ITooltipProvider() {

	    @Override
	    public String getToolTip(final TreeNode treeNode) {
		final TitledObject title = (TitledObject) ((DefaultMutableTreeNode) treeNode).getUserObject();
		final String propertyType = title.getType() != null ? (Date.class.isAssignableFrom(title.getType()) ? "distribution" : "aggregation") : "";
		return "<html>Click here to set " + propertyType + " function for <b>" + extractToolTipText(treeNode, false) + "</b> property</html>";
	    }

	};
    }

    private static ITreeItemFilter createFilterForAggregationProperties() {
	return new ITreeItemFilter() {

	    @Override
	    public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {
		if (treeNode.getPath().length <= 1) {
		    return true;
		}
		final TitledObject title = (TitledObject) treeNode.getUserObject();
		if (Reflector.isSynthetic(title.getType()) || title.getType() == null || Date.class.isAssignableFrom(title.getType())
			|| boolean.class.isAssignableFrom(title.getType())) {
		    return true;
		}
		return false;
	    }

	    @Override
	    public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
		return false;
	    }
	};
    }

    private static ITreeItemFilter createFilterForDistributionProperties() {
	return new ITreeItemFilter() {

	    @Override
	    public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {

		if (treeNode.getPath().length <= 2) {
		    return true;
		}
		final TitledObject title = (TitledObject) treeNode.getUserObject();

		if (title.getType() == null) {
		    return true;
		}

		if (EntityUtils.isEntityType(title.getType())){
		    final KeyType keyTypeAnnotation = AnnotationReflector.getAnnotation(KeyType.class, title.getType());
		    //disable properties those are entities with AE key or composite key.
		    return keyTypeAnnotation!=null && (EntityUtils.isEntityType(keyTypeAnnotation.value()) || DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value()));
		}
		if (Date.class.isAssignableFrom(title.getType())) {
		    return false;
		}
		if (boolean.class.isAssignableFrom(title.getType())) {
		    return false;
		}
		return true;
	    }

	    @Override
	    public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
		return false;
	    }
	};
    }
}
