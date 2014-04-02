package ua.com.fielden.platform.ndimcube;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * The model for multiple dimension cube.
 * 
 * @author TG Team
 * 
 */
public interface MultipleDimensionCubeModel {

    /**
     * Returns the {@link TreeModel} of the multiple dimension cube row.
     * 
     * @return
     */
    TreeModel getRowModel();

    /**
     * Returns the {@link TreeModel} of the multiple dimension cube column.
     * 
     * @return
     */
    TreeModel getColumnModel();

    /**
     * Returns the number of data dimension.
     * 
     * @return
     */
    int getValueColumnCount();

    /**
     * Returns the name of the specified data dimension.
     * 
     * @param valueIndex
     *            - the data dimension index for which the name must be retrieved.
     * @return
     */
    String getValueColumnName(int valueIndex);

    /**
     * returns the tool tip of the specified data dimension.
     * 
     * @param valueIndex
     *            - the data dimension index for which the tool tip must be retrieved.
     * @return
     */
    String getValueColumnToolTip(int valueIndex);

    /**
     * Returns the type of the specified data dimension.
     * 
     * @param valueIndex
     *            - the data dimension index for which the value type must be retrieved.
     * @return
     */
    Class<?> getValueColumnClass(int valueIndex);

    /**
     * Returns the value for specified the multidimensional row, column and data dimension index.
     * 
     * @param row
     * @param column
     * @param valueIndex
     * @return
     */
    Object getValueAt(TreeNode row, TreeNode column, int valueIndex);

    /**
     * Set the specified value for multidimensional row, column and data dimension index. Throws {@link IllegalArgumentException} if the specified value is not of the correct value
     * type determined with {@link #getValueType(int)}.
     * 
     * @param row
     * @param column
     * @param valueIndex
     * @param value
     */
    void setValueAt(TreeNode row, TreeNode column, int valueIndex, Object value);

}