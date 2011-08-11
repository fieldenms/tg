package ua.com.fielden.platform.swing.menu.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;

/**
 * An Abstract class that wraps the original tree model in order to provide the ability to filter it.
 * 
 * @author oleh
 * 
 */
public abstract class AbstractFilterableTreeModel implements TreeModel, IFilterableModel {
    /**
     * The original model wrapped in for filtering
     */
    private final TreeModel model;
    // needed for specifying special properties for visualising tree
    private boolean andMode;
    private boolean keepAllChildren;
    private boolean hideEmptyParentNode;
    private boolean matchesLeafNodeOnly;

    // need temporary to identify whether filtering process should be break or not
    private boolean isBreak = false;

    /**
     * List of filters for tree filtering
     */
    private final List<IFilter> filters = new ArrayList<IFilter>();
    /**
     * A set of filter listeners, which get notified upon filtering.
     */
    private final Set<IFilterListener> filterListeners = new HashSet<IFilterListener>();
    /**
     * a set of listeners those are notified if the filtering process must be break
     */
    private final Set<IFilterBreakListener> filterBreakListener = new HashSet<IFilterBreakListener>();
    /**
     * Maps all tree nodes to their visibility attribute as determined dufing filtering.
     */
    private final Map<TreeNode, Boolean> visibilityMap = new HashMap<TreeNode, Boolean>();

    private String lastFilter;

    /**
     * Instantiates filterable tree model wrapper where filters are chained with <code>AND</code> condition.
     * 
     * @param model
     *            -- tree model to wrap.
     */
    public AbstractFilterableTreeModel(final TreeModel model) {
	this(model, true);
    }

    /**
     * Instantiates filterable tree model wrapper.
     * 
     * @param model
     *            -- tree model to wrap.
     * @param andMode
     *            -- specifies whether filters should be chained with <code>AND</code> condition
     */
    public AbstractFilterableTreeModel(final TreeModel model, final boolean andMode) {
	this.model = model;
	setAndMode(andMode);
    }

    /**
     * Handles search for a child node by index with respect to the applied filter.
     */
    @Override
    public TreeNode getChild(final Object parent, final int index) {
	int actualIndex = -1;
	final TreeNode treeNode = (TreeNode) parent;

	for (int childCounter = 0; childCounter < treeNode.getChildCount(); childCounter++) {
	    final TreeNode childNode = treeNode.getChildAt(childCounter);
	    if (!visibilityMap.containsKey(childNode)) {
		visibilityMap.put(childNode, true);
	    }
	    if (visibilityMap.get(childNode)) {
		actualIndex++;
		if (index == actualIndex) {
		    return childNode;
		}
	    }
	}
	return null;
    }

    @Override
    public int getChildCount(final Object parent) {
	int counter = 0;
	final TreeNode treeNode = (TreeNode) parent;
	for (int childIndex = 0; childIndex < treeNode.getChildCount(); childIndex++) {
	    final TreeNode childNode = treeNode.getChildAt(childIndex);
	    if (!visibilityMap.containsKey(childNode)) {
		visibilityMap.put(childNode, true);
	    }
	    if (visibilityMap.get(childNode)) {
		counter++;
	    }
	}
	return counter;
    }

    /**
     * Tests node's visibility and results its index if visible.
     */
    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
	int index = -1;
	final TreeNode treeNode = (TreeNode) parent;
	for (int childIndex = 0; childIndex < treeNode.getChildCount(); childIndex++) {
	    final TreeNode childNode = treeNode.getChildAt(childIndex);
	    if (!visibilityMap.containsKey(childNode)) {
		visibilityMap.put(childNode, true);
	    }
	    if (visibilityMap.get(childNode)) {
		index++;
	    }
	    if(childNode.equals(child)){
		return index;
	    }
	}
	return index;
    }

    @Override
    public TreeNode getRoot() {
	return (TreeNode) getOriginModel().getRoot();
    }

    @Override
    public void filter(final String value) {
	lastFilter = value;
	isBreak = false;
	for (final IFilterListener listener : filterListeners) {
	    listener.preFilter(this);
	}
	final TreeNode rootNode = getRoot();
	// The root node should not really be filtered
	// Thus, filtering should start the root's children
	// perform filtering
	for (int index = 0; index < rootNode.getChildCount(); index++) {
	    filterTree(rootNode.getChildAt(index), value);
	}

	reload(); // notifies tree of structural changes
	// notify all filter listeners

	for (final IFilterListener listener : filterListeners) {
	    listener.postFilter(this);
	}

	if (isBreak) {
	    for (final IFilterBreakListener listener : filterBreakListener) {
		listener.doAfterBreak(this);
	    }
	}

    }

    public boolean matches(final TreeNode node) {
	return StringUtils.isEmpty(lastFilter) ? false : !applyFilter(node, lastFilter);
    }

    private void filterTree(final TreeNode treeNode, final String value) {
	if (isBreak) {
	    return;
	}
	final boolean filterRes = applyFilter(treeNode, value);
	if (!visibilityMap.containsKey(treeNode)) {
	    visibilityMap.put(treeNode, true);
	}
	final boolean oldValue = visibilityMap.get(treeNode);
	visibilityMap.put(treeNode, !filterRes);

	// visibility of the parent node is driven by leaf nodes
	if (isMatchesLeafNodeOnly() && !treeNode.isLeaf()) {
	    visibilityMap.put(treeNode, true);
	}

	// if this node is visible then all its children should also be
	// visible if model is configured to keep children and filtering is not only by leaf nodes
	if (isKeepAllChildren() && !filterRes && !isMatchesLeafNodeOnly()) {
	    setChildrenVisible(treeNode, true);
	} else { // otherwise need to traverse all the children
	    boolean atLeastOneChildIsVisible = false;
	    for (int childIndex = 0; childIndex < treeNode.getChildCount(); childIndex++) {
		final TreeNode childNode = treeNode.getChildAt(childIndex);
		filterTree(childNode, value);
		if (visibilityMap.get(childNode)) {
		    atLeastOneChildIsVisible = true;
		}
	    }
	    // a tree node, which is not a leaf, should be always visible if empty parents should not be hidden
	    // and filtering is by leaf nodes only
	    if (atLeastOneChildIsVisible) {
		visibilityMap.put(treeNode, true);
	    } else if (!atLeastOneChildIsVisible && isHideEmptyParentNode() && !treeNode.isLeaf() && isMatchesLeafNodeOnly()) {
		// a tree node, which is not a leaf, under condition that empty parents should not be hidden,
		// should be visible if one of its children is visible (atLeastOneChildIsVisible)
		visibilityMap.put(treeNode, false);
	    }
	}
	final boolean newValue = visibilityMap.get(treeNode);
	for (final IFilterListener listener : filterListeners) {
	    if (listener.nodeVisibilityChanged(treeNode, oldValue, newValue)) {
		isBreak = true;
	    }
	}
    }

    // tests the tree node if it satisfies the filter
    private boolean applyFilter(final TreeNode treeNode, final String value) {
	boolean result = isAndMode();
	for (final IFilter filter : filters) {
	    if (isAndMode()) {
		result = result && filter.filter(treeNode, value);
		if (!result) { // this just to avoid running through all filters
		    return false;
		}
	    } else {
		result = result || !filter.filter(treeNode, value);
	    }
	}
	return result;
    }

    private void setChildrenVisible(final TreeNode treeNode, final boolean filterRes) {
	visibilityMap.put(treeNode, filterRes);
	for (int childCounter = 0; childCounter < treeNode.getChildCount(); childCounter++) {
	    setChildrenVisible(treeNode.getChildAt(childCounter), filterRes);
	}
    }

    @Override
    public void addFilterListener(final IFilterListener listener) {
	filterListeners.add(listener);
    }

    @Override
    public void removeFilterListener(final IFilterListener listener) {
	filterListeners.remove(listener);
    }

    @Override
    public void addFilter(final IFilter filter) {
	filters.add(filter);
    }

    @Override
    public IFilter getFilter(final int index) {
	return filters.get(index);
    }

    @Override
    public boolean isAndMode() {
	return this.andMode;
    }

    @Override
    public void removeFilter(final IFilter filter) {
	filters.remove(filter);
    }

    @Override
    public IFilter removeFilter(final int index) {
	return filters.remove(index);
    }

    @Override
    public void setAndMode(final boolean andMode) {
	this.andMode = andMode;
    }

    /**
     * Returns the value that indicates whether tree model shouldn't remove all children if their parent satisfies filter or should remove otherwise
     * 
     * @return
     */
    public boolean isKeepAllChildren() {
	return keepAllChildren;
    }

    /**
     * Sets the keepAllChildren property for the model see the {@link #isKeepAllChildren()} for more details about that property
     * 
     * @param keepAllChildren
     */
    public void setKeepAllChildren(final boolean keepAllChildren) {
	this.keepAllChildren = keepAllChildren;
    }

    /**
     * Returns the value that indicates whether the empty nodes of the tree should be hidden or not
     * 
     * @return
     */
    public boolean isHideEmptyParentNode() {
	return hideEmptyParentNode;
    }

    /**
     * Sets the hideEmptyParrentNode property for the tree model. See the {@link #isHideEmptyParentNode()} for more information about that property
     * 
     * @param hideEmptyParentNode
     */
    public void setHideEmptyParentNode(final boolean hideEmptyParentNode) {
	this.hideEmptyParentNode = hideEmptyParentNode;
    }

    /**
     * Returns the value that indicates whether only leaf nodes must be filtered or not
     * 
     * @return
     */
    public boolean isMatchesLeafNodeOnly() {
	return matchesLeafNodeOnly;
    }

    /**
     * Sets the matchesOnlyLeafNodes property for the tree model. See the {@link #isMatchesLeafNodeOnly()} for more information about that property
     * 
     * @param matchesLeafNodeOnly
     */
    public void setMatchesLeafNodeOnly(final boolean matchesLeafNodeOnly) {
	this.matchesLeafNodeOnly = matchesLeafNodeOnly;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
	getOriginModel().addTreeModelListener(l);
    }

    @Override
    public boolean isLeaf(final Object node) {
	return getOriginModel().isLeaf(node);
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
	getOriginModel().removeTreeModelListener(l);
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
	getOriginModel().valueForPathChanged(path, newValue);
    }

    public String getLastFilter() {
	return lastFilter;
    }

    /**
     * Returns the original wrapped tree model
     * 
     * @return
     */
    public TreeModel getOriginModel() {
	return model;
    }

    @Override
    public void addFilterBreakListener(final IFilterBreakListener listener) {
	filterBreakListener.add(listener);
    }

    @Override
    public void removeFilterBreakListener(final IFilterBreakListener listener) {
	filterBreakListener.remove(listener);
    }

    /**
     * Reloads the tree model after the filtering
     */
    public abstract void reload();
}
