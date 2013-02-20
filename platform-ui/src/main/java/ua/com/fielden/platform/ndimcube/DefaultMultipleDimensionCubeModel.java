package ua.com.fielden.platform.ndimcube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * The default implementation of the {@link MultipleDimensionCubeModel}. Allows to create four types of multiple dimension cube:
 * First creates multiple dimension cube with hierarchical row and column. The other two create multiple dimension cube either with hierarchical row or column.
 * And the fourth one creates empty multiple dimension cube model. This model also allows to reset column and row models.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DefaultMultipleDimensionCubeModel implements MultipleDimensionCubeModel {

    private final DefaultTreeModel rowModel, columnModel;
    private final Map<Pair<TreeNode, TreeNode>, Map<Object, Object>> data = new HashMap<>();
    private final Map<Object, String> valueName = new HashMap<>();
    private final Map<Object, String> valueTip = new HashMap<>();
    private final Map<Object, Class<?>> valueClass = new HashMap<>();
    private final List<Object> valueIdentifiers = new ArrayList<>();

    /**
     * Creates and returns the {@link DefaultMultipleDimensionCubeModel} with specified row and column models.
     *
     * @param rowModel
     * @param columnModel
     * @return
     */
    public static DefaultMultipleDimensionCubeModel createMultipleDimensionCube(//
	    final DefaultTreeModel rowModel,//
	    final DefaultTreeModel columnModel) {
	return new DefaultMultipleDimensionCubeModel(rowModel, columnModel);
    }

    /**
     * Creates and returns the {@link DefaultMultipleDimensionCubeModel} with specified row model.
     *
     * @param rowModel
     * @return
     */
    public static DefaultMultipleDimensionCubeModel createMultipleDimensionCubeWithRow(//
	    final DefaultTreeModel rowModel) {
	return new DefaultMultipleDimensionCubeModel(rowModel, null);
    }

    /**
     * Creates and returns the {@link DefaultMultipleDimensionCubeModel} with specified column model.
     *
     * @param columnModel
     * @return
     */
    public static DefaultMultipleDimensionCubeModel createMultipleDimensionCubeWithColumn(//
	    final DefaultTreeModel columnModel) {
	return new DefaultMultipleDimensionCubeModel(null, columnModel);
    }

    /**
     * Creates and returns the empty {@link DefaultMultipleDimensionCubeModel}.
     *
     * @return
     */
    public static DefaultMultipleDimensionCubeModel createEmptyMultipleDimensionCube() {
	return new DefaultMultipleDimensionCubeModel(null, null);
    }

    /**
     * Constructs the multiple dimension cube model with specified row and column tree models.
     *
     * @param rowModel
     * @param columnModel
     */
    protected DefaultMultipleDimensionCubeModel(//
	    final DefaultTreeModel rowModel,//
	    final DefaultTreeModel columnModel) {
	this.rowModel = rowModel;
	this.columnModel = columnModel;
	this.data.clear();
	this.valueName.clear();
	this.valueTip.clear();
	this.valueClass.clear();
	this.valueIdentifiers.clear();
    }

    @Override
    public DefaultTreeModel getRowModel() {
	return rowModel;
    }

    @Override
    public DefaultTreeModel getColumnModel() {
	return columnModel;
    }

    @Override
    public int getValueColumnCount() {
	return valueIdentifiers.size();
    }

    @Override
    public String getValueColumnName(final int valueIndex) {
	return valueName.get(valueIdentifiers.get(valueIndex));
    }

    @Override
    public String getValueColumnToolTip(final int valueIndex) {
	return valueTip.get(valueIdentifiers.get(valueIndex));
    }

    @Override
    public Class<?> getValueColumnClass(final int valueIndex) {
	return valueClass.get(valueIdentifiers.get(valueIndex));
    }

    @Override
    public Object getValueAt(final TreeNode row, final TreeNode column, final int valueIndex) {
	if (EntityUtils.equalsEx(rowModel == null ? null : rowModel.getRoot(), getRoot(row)) //
		&& EntityUtils.equalsEx(columnModel == null ? null : columnModel.getRoot(), getRoot(column))) {
	    final Map<Object, Object> values = data.get(new Pair<>(row, column));
	    return values != null ? values.get(valueIdentifiers.get(valueIndex)) : null;
	}
	return null;
    }

    @Override
    public void setValueAt(final TreeNode row, final TreeNode column, final int valueIndex, final Object value) {
	if (EntityUtils.equalsEx(rowModel == null ? null : rowModel.getRoot(), getRoot(row)) //
		&& EntityUtils.equalsEx(columnModel == null ? null : columnModel.getRoot(), getRoot(column))) {
	    final Pair<TreeNode, TreeNode> key = new Pair<>(row, column);
	    Map<Object, Object> values = data.get(key);
	    if (values == null) {
		values = new HashMap<>();
		data.put(key, values);
	    }
	    values.put(valueIdentifiers.get(valueIndex), value);
	}
    }

    /**
     * Adds new column with specified identifier.
     * If column with given identifier exists, then it will throw {@link IllegalArgumentException}.
     *
     * @param identifier
     */
    public DefaultMultipleDimensionCubeModel addValueColumn(final Object identifier) {
	if (!valueIdentifiers.contains(identifier)) {
	    valueIdentifiers.add(identifier);
	    return this;
	}
	throw new IllegalArgumentException("The column with " + identifier + " identifier already exists");
    }

    /**
     * Adds new column with specified identifier at position index.
     * If column with given identifier exists, then it will throw {@link IllegalArgumentException}.
     *
     * @param identifier
     * @param index
     * @return
     */
    public DefaultMultipleDimensionCubeModel addValueColumn(final Object identifier, final int index) {
	if (!valueIdentifiers.contains(identifier)) {
	    valueIdentifiers.add(index, identifier);
	    return this;
	}
	throw new IllegalArgumentException("The column with " + identifier + " identifier already exists");
    }

    /**
     * Set the column name.
     *
     * @param identifier - identifier of the column for which the name must be set.
     * @param name
     * @return
     */
    public DefaultMultipleDimensionCubeModel setColumnName(final Object identifier, final String name){
	if(valueIdentifiers.contains(identifier)){
	    valueName.put(identifier, name);
	    return this;
	}
	throw new IllegalArgumentException("The column with " + identifier + " identifier, doesn't exists");
    }

    /**
     * Set the column tool tip.
     *
     * @param identifier - identifier of the column for which the tool tip must be set.
     * @param toolTip
     * @return
     */
    public DefaultMultipleDimensionCubeModel setColumnToolTip(final Object identifier, final String toolTip){
	if(valueIdentifiers.contains(identifier)){
	    valueTip.put(identifier, toolTip);
	    return this;
	}
	throw new IllegalArgumentException("The column with " + identifier + " identifier, doesn't exists");
    }

    /**
     * Set the column class.
     *
     * @param identifier - identifier of the column for which the class must be set.
     * @param type
     * @return
     */
    public DefaultMultipleDimensionCubeModel setColumnClass(final Object identifier, final Class<?> type){
	if(valueIdentifiers.contains(identifier)){
	    valueClass.put(identifier, type);
	    return this;
	}
	throw new IllegalArgumentException("The column with " + identifier + " identifier, doesn't exists");
    }

    /**
     * Removes the value column specified with identifier.
     *
     * @param identifier
     * @param withRelatedInfo - determines whether to remove other information (e.a. name, tool tip and value type, value) related to the identifier.
     * @return
     */
    public boolean removeValueColumn(final Object identifier, final boolean withRelatedInfo) {
	final boolean result = valueIdentifiers.remove(identifier);
	if(result && withRelatedInfo){
	    valueName.remove(identifier);
	    valueTip.remove(identifier);
	    valueClass.remove(identifier);
	    removeValuesFor(identifier);
	    return true;
	}
	return result;
    }

    /**
     * Moves the value column fromIndex to toIndex.
     *
     * @param fromIndex
     * @param toIndex
     */
    public void moveColumnTo(final int fromIndex, final int toIndex){
	final Object identifier = valueIdentifiers.remove(fromIndex);
	valueIdentifiers.add(toIndex, identifier);
    }

    /**
     * Returns the column identifier at specified index.
     *
     * @param index
     * @return
     */
    public Object getColumnIdentifier(final int index){
	return valueIdentifiers.get(index);
    }

    /**
     * Returns the identifier index.
     *
     * @param identifier
     * @return
     */
    public int getIdentifierIndex(final Object identifier){
	return valueIdentifiers.indexOf(identifier);
    }

    /**
     * Removes values from data those corresponds to the given identifier.
     *
     * @param identifier
     */
    private void removeValuesFor(final Object identifier) {
	for(final Map<Object, Object> valueEntry : data.values()){
	    valueEntry.remove(identifier);
	}
    }

    /**
     * Returns the top most parent (e.a. root) for the specified node;
     *
     * @param node
     * @return
     */
    private TreeNode getRoot(final TreeNode node) {
	TreeNode tempNode = node;
	while (tempNode != null && tempNode.getParent() != null) {
	    tempNode = tempNode.getParent();
	}
	return tempNode;
    }
}
