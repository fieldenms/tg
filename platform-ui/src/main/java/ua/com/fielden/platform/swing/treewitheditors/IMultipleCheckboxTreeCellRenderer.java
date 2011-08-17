package ua.com.fielden.platform.swing.treewitheditors;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.treemodel.rules.ITooltipProvider;

/**
 * The {@link TreeCellRenderer} for the {@link MultipleCheckboxTree}
 * 
 * @author oleh
 * 
 */
public interface IMultipleCheckboxTreeCellRenderer extends TreeCellRenderer {

    /**
     * Determines the index of the component that contain the point specified with x- and y- coordinates
     * 
     * @param x
     *            - x-coordinate of the specified point
     * @param y
     *            - y-coordinate of the specified point
     * 
     * 
     * @return returns the index of component or -1 if there is no component that contain the point specified with x- and y- coordinates
     */
    int getHotspotIndex(final int x, final int y);

    /**
     * Performs any mouse action that occurred above component specified with checkingComponentIndex parameter.
     * 
     * @param treePath
     *            - {@link TreePath} instance for the tree node where mouse click action occurred.
     * @param checkingComponentIndex
     *            - index of the component above which action occurred.
     */
    void performMouseAction(final TreePath treePath, final int checkingComponentIndex);

    /**
     * Returns the {@link ITooltipProvider} of the {@link IMultipleCheckboxTreeCellRenderer} associated with this {@link MultipleCheckboxTree}
     * 
     * @return
     */
    ITooltipProvider getToolTipProvider();

    /**
     * Returns {@link ITooltipProvider} of the {@link ITreeCheckingModelComponent} specified with index
     * 
     * @param checkBoxIndex
     * @return
     */
    ITooltipProvider getToolTipProvider(final int checkBoxIndex);
}
