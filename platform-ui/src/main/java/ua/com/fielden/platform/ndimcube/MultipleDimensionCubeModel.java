package ua.com.fielden.platform.ndimcube;

import javax.swing.tree.TreeModel;

/**
 * The model for multiple dimension cube.
 *
 * @author TG Team
 *
 */
public interface MultipleDimensionCubeModel<T> {

    /**
     * Returns the {@link TreeModel} of the multiple dimension cube row.
     *
     * @return
     */
    MultipleDimensionCubeTreeModel<T> getRowModel();

    /**
     * Returns the {@link TreeModel} of the multiple dimension cube column.
     *
     * @return
     */
    MultipleDimensionCubeTreeModel<T> getColumnModel();

    /**
     * Returns the number of data dimension.
     *
     * @return
     */
    int getValueCount();

    /**
     * Returns the name of the specified data dimension.
     *
     * @param valueIndex - the data dimension index for which the name must be retrieved.
     * @return
     */
    int getValueName(int valueIndex);

    /**
     * Returns the type of the specified data dimension.
     *
     * @param valueIndex - the data dimension index for which the value type must be retrieved.
     * @return
     */
    Class<?> getValueType(int valueIndex);

    /**
     * Returns the value for specified the multidimensional row, column and data dimension index.
     *
     * @param row
     * @param column
     * @param valueIndex
     * @return
     */
    Object getValueAt(MultipleDimensionCubeTreeNode<T> row, MultipleDimensionCubeTreeNode<T> column, int valueIndex);

    /**
     * Set the specified value for multidimensional row, column and data dimension index.
     * Throws {@link IllegalArgumentException} if the specified value is not of the correct value type
     * determined with {@link #getValueType(int)}.
     *
     * @param row
     * @param column
     * @param valueIndex
     * @param value
     */
    void setValueAt(MultipleDimensionCubeTreeNode<T> row, MultipleDimensionCubeTreeNode<T> column, int valueIndex, Object value);
}