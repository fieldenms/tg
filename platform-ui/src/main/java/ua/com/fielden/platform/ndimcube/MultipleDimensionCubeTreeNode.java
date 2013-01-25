package ua.com.fielden.platform.ndimcube;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

/**
 * The {@link TreeNode} for multiple dimension cube.
 *
 * @author TG Team
 *
 */
public interface MultipleDimensionCubeTreeNode<T> extends TreeNode {

    /**
     * Returns the value for the specified multidimensional node and value index.
     *
     * @param node
     * @param valueIndex
     * @return
     */
    Object getValueAt(MultipleDimensionCubeTreeNode<T> node, int valueIndex);

    /**
     * Set the specified value for multidimensional node and value index.
     *
     * @param node
     * @param valueIndex
     * @param value
     */
    void setValueAt(MultipleDimensionCubeTreeNode<T> node, int valueIndex, Object value);

    /**
     * Returns this node's user object.
     *
     * @return the object that user specifies for this node.
     */
    T getUserObject();

    /**
     * Set this node's user object
     *
     * @param userObject
     */
    void setUserObejct(T userObject);

    /**
     * Returns the number of data dimensions.
     *
     * @return
     */
    int getValueCount();

    @Override
    Enumeration<? extends MultipleDimensionCubeTreeNode<T>> children();

    @Override
    MultipleDimensionCubeTreeNode<T> getChildAt(int index);

    @Override
    MultipleDimensionCubeTreeNode<T> getParent();
}
