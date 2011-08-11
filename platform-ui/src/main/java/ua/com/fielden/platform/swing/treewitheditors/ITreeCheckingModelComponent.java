package ua.com.fielden.platform.swing.treewitheditors;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * Represents the component on the {@link MultipleCheckboxTree} item
 * 
 * @author oleh
 * 
 */
public interface ITreeCheckingModelComponent {

    /**
     * Returns the component that must be placed on the tree item
     * 
     * @return
     */
    Component getComponent();

    /**
     * Updates the components state according to the changes made in the {@link MultipleCheckboxTree}
     * 
     * @param tree
     * @param value
     * @param selected
     * @param expanded
     * @param leaf
     * @param row
     * @param hasFocus
     */
    void updateComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus);

    /**
     * Determines whether the pint specified with x and y coordinates is on the component
     * 
     * @param x
     *            - x coordinate of the point
     * @param y
     *            - y coordinate of the point
     * @return
     */
    boolean isOnHotspot(final int x, final int y);

    /**
     * Performs any action on mouse click event.
     * 
     * @param treePath
     *            - tree path for the tree item on which mouse was clicked.
     */
    void actionPerformed(TreePath treePath);
}
