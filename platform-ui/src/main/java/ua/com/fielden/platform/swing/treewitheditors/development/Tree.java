package ua.com.fielden.platform.swing.treewitheditors.development;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.treetable.ViewportChangeEvent;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Tree with common expanding/collapsing logic.
 *
 * @author Jhou
 *
 */
public class Tree extends JTree {
    private static final long serialVersionUID = 3213197322869746054L;

    // expanding/collapsing actions:
    private final Action expandAllAction = createExpandAllAction();
    private final Action collapseAllAction = createCollapseAllAction();
    private final Action collapseAllExceptSelectedAction = createCollapseAllExceptSelectedAction();

    /**
     * Holds current viewport's position.
     */
    private Point viewportPosition = new Point(0, 0);
    /**
     * Holds current view's size.
     */
    private Dimension viewportSize = new Dimension(0, 0);

    public Tree(final TreeModel defaultTreeModel) {
	super(defaultTreeModel);
    }

    public Tree(final TreeNode root) {
	super(root);
    }

    public void bringSelectedIntoView() {
	scrollPathToVisible(getSelectionPath());
    }


    /**
     * Collapses all nodes that can be collapsed. There can be at most one node that cannot be collapsed, which is determined by its <code>canClose</code> method.
     *
     * @return
     */
    public Tree collapseAll() {
	for (int rowCounter = getRowCount(); rowCounter >= 0; rowCounter--) {
	    final TreePath path = getPathForRow(rowCounter);
	    if (shouldCollapse(path)) {
		collapseRow(rowCounter);
	    }
	}
	return this;
    }

    /**
     * Collapse all but selected node.
     *
     * @return
     */
    public Tree collapseAllExceptSelected() {
	for (int rowCounter = getRowCount(); rowCounter >= 0; rowCounter--) {
	    final TreePath path = getPathForRow(rowCounter);
	    final TreePath selectedPath = getSelectionPath();
	    if (path != null && selectedPath != null) {
		if ((!path.isDescendant(selectedPath) || (selectedPath.isDescendant(path))) && shouldCollapse(path)) {
		    collapseRow(rowCounter);
		}
	    }
	}
	return this;
    }

    /**
     * Determines whether the {@code treePath} can be collapsed or not
     *
     * @param treePath
     * @return
     */
    public boolean shouldCollapse(final TreePath treePath) {
	return true;
    }

    public Action getExpandAllAction() {
	return expandAllAction;
    }

    public Action getCollapseAllAction() {
	return collapseAllAction;
    }

    public Action getCollapseAllExceptSelectedAction() {
	return collapseAllExceptSelectedAction;
    }

    // ///////////////////////// Collapsing/expanding and related actions: /////////////////////////
    private Action createExpandAllAction() {
	final Action action = new Command<Void>("") {

	    private static final long serialVersionUID = 344489827335865704L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		expandAll();
	    }

	};

	final Icon icon = ResourceLoader.getIcon("images/plus.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Expand all items");
	return action;
    }

    private Action createCollapseAllAction() {
	final Action action = new Command<Void>("") {
	    private static final long serialVersionUID = 344489827335865704L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		collapseAll();
	    }

	};

	final Icon icon = ResourceLoader.getIcon("images/minus.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Collaps all items");
	return action;
    }

    private Action createCollapseAllExceptSelectedAction() {
	final Action action = new Command<Void>("") {
	    private static final long serialVersionUID = 344489827335865704L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		collapseAllExceptSelected();
	    }

	};

	final Icon icon = ResourceLoader.getIcon("images/tree.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Collapse all except selected item");
	return action;
    }

    /////////////////Incremental expanding./////////////////////

    /**
     * Expands all tree nodes of the tree
     *
     * @return
     */
    public Tree expandAll() {
	final List<TreePath> expandPaths = new ArrayList<TreePath>();
	TreePath startPath=getClosestPathForLocation(viewportPosition.x, viewportPosition.y);
	startPath = startPath == null ? getClosestPathForLocation(0, 0) : startPath;
	if(startPath!=null){
	    collectPathsToExpand(startPath, expandPaths, getViewportRowCount(getRowBounds(getRowForPath(startPath)).height));
	    for(final TreePath treePath:expandPaths){
		expandPath(treePath);
	    }
	}
	return this;
    }

    /**
     * Returns list of <code>numOfPaths</code> paths those starts from specified startPath. Uses depth-first search to build the paths.<br>
     * Throws NullPointerException if startPath is null.
     *
     * @param startPath
     * @param pathsToExpand
     * @param numOfPaths
     * @return
     */
    private boolean collectPathsToExpand(final TreePath startPath, final List<TreePath> pathsToExpand, final int numOfPaths) {
	if (pathsToExpand.size() >= numOfPaths) {
	    return true;
	} else {
	    if (!canExpand(startPath.getLastPathComponent())) {
		return traceNextAvailablePath(startPath, pathsToExpand, numOfPaths - 1);
	    }
	    pathsToExpand.add(startPath);
	    final Object treeNode = startPath.getLastPathComponent();
	    final Object child = getModel().getChildCount(treeNode) == 0 ? null : getModel().getChild(treeNode, 0);
	    if (child == null) {
		return traceNextAvailablePath(startPath, pathsToExpand, numOfPaths);
	    } else {
		return collectPathsToExpand(startPath.pathByAddingChild(child), pathsToExpand, numOfPaths);
	    }
	}
    }

    /**
     * Convenient method that collects paths to expand for next available tree path for start path.
     *
     * @param startPath - the path for which available one should be found.
     * @param pathsToExpand - the list of paths to expand.
     * @param numOfPaths - number of paths those must be expanded.
     * @return
     */
    private boolean traceNextAvailablePath(final TreePath startPath, final List<TreePath> pathsToExpand, final int numOfPaths) {
	final TreePath availablePath = getNextAvailablePath(startPath);
	return availablePath == null ? false : collectPathsToExpand(availablePath, pathsToExpand, numOfPaths);
    }

    /**
     * Returns value that indicates whether specified node can be expanded or not.
     *
     * @param lastPathComponent
     * @return
     */
    protected boolean canExpand(final Object lastPathComponent) {
	return true;
    }

    /**
     * Returns next available {@link TreePath} for specified one. This method uses back tracking to identify available path.
     *
     * @param startPath
     * @return
     */
    private TreePath getNextAvailablePath(final TreePath startPath) {
	TreePath path=startPath;
	while(path.getParentPath()!=null){
	    final TreePath parentPath=path.getParentPath();
	    final Object lastComponent= path.getLastPathComponent();
	    final int childIndex = getModel().getIndexOfChild(parentPath.getLastPathComponent(), lastComponent);
	    final Object treeNode = parentPath.getLastPathComponent();
	    final Object child = getModel().getChildCount(treeNode) <= childIndex + 1 ? null : getModel().getChild(treeNode, childIndex+1);
	    if(child==null){
		path=parentPath;
	    }else{
		return parentPath.pathByAddingChild(child);
	    }
	}
	return null;
    }

    /**
     * Returns the number of rows visible in the viewport.
     *
     * @return
     */
    private int getViewportRowCount(final int rowHeight){
	return (int)Math.floor(viewportSize.getHeight()/rowHeight);
    }

    /**
     * Override in order to handle viewport change events. This method will be triggered when scroll pane changes it's viewport position or size.
     */
    public void viewPortChanged(final ViewportChangeEvent e) {
	this.viewportPosition=e.getViewportPosition();
	this.viewportSize = e.getSize();
    }
}
