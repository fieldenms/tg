package ua.com.fielden.platform.swing.filteredtree;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ua.com.fielden.platform.swing.menu.FilterCellRenderer;
import ua.com.fielden.platform.swing.menu.filter.FilterableTreeModel;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.treewitheditors.Tree;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;

/**
 * {@link JTree} that supports filtering.
 * 
 * @author oleh
 * 
 */
public class FilterableTree extends Tree{
    private static final long serialVersionUID = 3587126586223668990L;

    private FilterableTreeModel model;

    /**
     * Creates new {@link FilterableTree} with specified {@link IFilter}. Also it may select first item of the tree if the shouldSelectFirstMenuItem parameter is true. If the
     * rootVisible parameter is true then root of the tree will be visible otherwise not.
     * 
     * @param root
     *            - The root of the tree.
     * @param filter
     * @param shouldSelectFirstMenuItem
     * @param rootVisible
     */
    public FilterableTree(final TreeNode root, final IFilter filter, final boolean shouldSelectFirstMenuItem, final boolean rootVisible) {
	super(root);
	configureTree(filter, shouldSelectFirstMenuItem, rootVisible);
    }

    /**
     * See {@link #FilterableTree(TreeNode, IFilter, boolean, boolean)}.
     * 
     * @param treeModel
     *            - specified {@link TreeModel} of the tree.
     * @param filter
     * @param shouldSelectFirstItem
     * @param rootVisible
     */
    public FilterableTree(final DefaultTreeModel treeModel, final IFilter filter, final boolean shouldSelectFirstItem, final boolean rootVisible) {
	super(treeModel);
	configureTree(filter, shouldSelectFirstItem, rootVisible);
    }

    /**
     * Creates {@link FilterableTreeModel} for this tree and initialize it with appropriate {@link IFilterListener}s.
     */
    private void configureTree(final IFilter filter, final boolean shouldSelectFirstItem, final boolean rootVisible) {
	// wrap the model
	model = new FilterableTreeModel((DefaultTreeModel) super.getModel());
	model.addFilter(filter);
	setModel(model);

	// set root visibility according to the rootVisible parameter
	setRootVisible(rootVisible);

	// add the filter listener
	getModel().addFilterListener(new IFilterListener() {
	    private TreePath prevSelected;

	    @Override
	    public void postFilter(final IFilterableModel model) {
		expandAll();
		FilterableTree.this.setSelectionPath(prevSelected);
	    }

	    @Override
	    public boolean nodeVisibilityChanged(final TreeNode treeNode, final boolean prevValue, final boolean newValue) {
		return false;
	    }

	    @Override
	    public void preFilter(final IFilterableModel model) {
		prevSelected = getSelectionPath();
	    }
	});

	addTreeWillExpandListener(new TreeWillExpandListener() {
	    @Override
	    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
		if (getModel().getOriginModel() instanceof EntitiesTreeModel) {
		    ((EntitiesTreeModel) getModel().getOriginModel()).loadProperties((DefaultMutableTreeNode) event.getPath().getLastPathComponent());
		}
	    }

	    @Override
	    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
	    }
	});

	setCellRenderer(new FilterCellRenderer(getModel()));
	final TreeSelectionModel selectionModel = getSelectionModel();
	selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	if (shouldSelectFirstItem) {
	    setSelectionRow(0);
	}
    }

    @Override
    public FilterableTreeModel getModel() {
	return model;
    }


}
