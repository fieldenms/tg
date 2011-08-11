package ua.com.fielden.platform.swing.menu.filter;

import javax.swing.tree.TreeNode;

/**
 * A contract to be implemented when it is necessary to handle filtering events produced by {@link FilterableTreeModel} upon filtering.
 * 
 * @author 01es
 * 
 */
public interface IFilterListener {

    void preFilter(IFilterableModel model);

    void postFilter(IFilterableModel model);

    boolean nodeVisibilityChanged(TreeNode treeNode, boolean prevValue, boolean newValue);
}
