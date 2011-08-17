package ua.com.fielden.platform.treemodel.rules;

import javax.swing.tree.TreeNode;

/**
 * Provides contract that allows to retrieve tool tips for the tree item
 * 
 * 
 * @author oleh
 * 
 */
public interface ITooltipProvider {

    /**
     * Returns the tool tip text for the given {@link TreeNode}.
     * 
     * @param treeNode
     *            - specified {@link TreeNode} for which tool tip text must be returned.
     * @return
     */
    String getToolTip(final TreeNode treeNode);
}
