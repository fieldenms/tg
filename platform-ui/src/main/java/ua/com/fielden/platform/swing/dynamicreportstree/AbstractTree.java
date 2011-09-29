package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.domaintree.ITooltipProvider;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPropertyAnalyser;
import ua.com.fielden.platform.swing.treewitheditors.CheckBoxTreeComponent;
import ua.com.fielden.platform.swing.treewitheditors.EntitiesTree;
import ua.com.fielden.platform.swing.treewitheditors.FilterMultipleCheckboxTreeCellRenderer;
import ua.com.fielden.platform.swing.treewitheditors.ITreeCheckingModelComponent;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;

/**
 * Abstract tree to be sub-classed for Analysis, Locator and Criteria.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractTree extends EntitiesTree {
    private static final long serialVersionUID = -7267339457181564682L;

    private final FilterMultipleCheckboxTreeCellRenderer renderer;

    public AbstractTree(final EntitiesTreeModel entitiesTreeModel) {
	this(entitiesTreeModel, createDefaultCriteriaColumnFilter(entitiesTreeModel), createDefaultTableHeaderColumnFilter(entitiesTreeModel));
    }

    public AbstractTree(final EntitiesTreeModel entitiesTreeModel, final ITreeItemFilter firstColumnFilter, final ITreeItemFilter secondColumnFilter) {
	super(entitiesTreeModel, "selection criteria", firstColumnFilter, secondColumnFilter);
	final List<ITooltipProvider> toolTipProviders = new ArrayList<ITooltipProvider>();
	toolTipProviders.add(createCriteriaCheckBoxToolTipProvider("selection criteria"));
	toolTipProviders.add(createResultantCheckBoxToolTipProvider("result set"));
	renderer = new FilterMultipleCheckboxTreeCellRenderer(this, this.getModel(), createLabelToolTipProvider(), toolTipProviders) {
	    private static final long serialVersionUID = 5620649799595619625L;

	    @Override
	    protected ITreeCheckingModelComponent getCheckingComponent(final TreeCheckingModel treeCheckingModel) {
		final int modelIndex = AbstractTree.this.getCheckingModelIndex(treeCheckingModel);
		return new CheckBoxTreeComponent(AbstractTree.this, modelIndex) {
		    @Override
		    public void actionPerformed(final TreePath treePath) {
		    }
		};
	    }
	};
	renderer.setPaintFocus(false);
	final AbstractTreeCellEditor editor = new AbstractTreeCellEditor(this, renderer);
	setCellRenderer(renderer);
	setEditable(true);
	setCellEditor(editor);
	final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	final String EDITING_MODE = "Start or stop editing selected path";
	getActionMap().put(EDITING_MODE, createEditingModeAction());
	getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, EDITING_MODE);
	expandRow(0);
    }

    private static ITreeItemFilter createDefaultCriteriaColumnFilter(final EntitiesTreeModel entitiesTreeModel) {
	return new ITreeItemFilter() {

	    @Override
	    public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {
		final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(//
		entitiesTreeModel.getPropertyType(treeNode), //
		entitiesTreeModel.getPropertyNameFor(treeNode), entitiesTreeModel.getPropertyFilter());
		if (!propertyAnalyser.canRemoveCriteraProperty() || !propertyAnalyser.isCriteriaPropertyAvailable()) {
		    return true;
		}
		return false;
	    }

	    @Override
	    public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
		final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(//
		entitiesTreeModel.getPropertyType(treeNode), //
		entitiesTreeModel.getPropertyNameFor(treeNode), entitiesTreeModel.getPropertyFilter());
		if (!propertyAnalyser.isCriteriaProertyChildrenAvailable()) {
		    return true;
		}

		//		final TitledObject titledObject = (TitledObject) treeNode.getUserObject();

		//		if (titledObject != null /*&& titledObject.isCollectional()*/) {
		//		    return true;
		//		} else {
		return false;
		//		}
	    }

	};
    }

    private static ITreeItemFilter createDefaultTableHeaderColumnFilter(final EntitiesTreeModel entitiesTreeModel) {
	return new ITreeItemFilter() {

	    @Override
	    public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {
		final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(//
		entitiesTreeModel.getPropertyType(treeNode), //
		entitiesTreeModel.getPropertyNameFor(treeNode), entitiesTreeModel.getPropertyFilter());
		if (!propertyAnalyser.isFetchPropertyAvailable()) {
		    return true;
		}
		return false;
	    }

	    @Override
	    public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
		final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(//
		entitiesTreeModel.getPropertyType(treeNode), //
		entitiesTreeModel.getPropertyNameFor(treeNode), entitiesTreeModel.getPropertyFilter());
		if (!propertyAnalyser.isFetchPropertyChildrenAvailable()) {
		    return true;
		}
		return false;
	    }

	};
    }

    private Action createEditingModeAction() {
	return new AbstractAction() {
	    private static final long serialVersionUID = -5565139442029535686L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (isEditing()) {
		    stopEditing();
		} else {
		    startEditingAtPath(getSelectionPath());
		}
	    }
	};
    }
}
