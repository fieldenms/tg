package ua.com.fielden.platform.ndimcube;

import javax.swing.tree.TreeModel;

/**
 * {@link TreeModel} for multidimensional cube's row and column.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface MultipleDimensionCubeTreeModel<T> extends TreeModel {

    @Override
    public MultipleDimensionCubeTreeNode<T> getRoot();

    @Override
    public MultipleDimensionCubeTreeNode<T> getChild(Object parent, int index);

}
