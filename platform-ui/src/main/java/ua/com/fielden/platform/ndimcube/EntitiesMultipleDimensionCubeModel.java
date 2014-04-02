package ua.com.fielden.platform.ndimcube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

public class EntitiesMultipleDimensionCubeModel<T extends AbstractEntity<?>> extends DefaultMultipleDimensionCubeModel {

    private final List<String> rowDistributionProperties = new ArrayList<>();
    private final List<String> columnDistributionProperties = new ArrayList<>();
    private final List<String> aggregationProperties = new ArrayList<>();

    //Needed temporary in order to load data.
    private final Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>> rowNodes = new HashMap<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>>();
    private final Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>> columnNodes = new HashMap<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>>();

    /**
     * Initialises empty multiple dimension cube model.
     */
    public EntitiesMultipleDimensionCubeModel() {
        super(new DefaultTreeModel(new DefaultMutableTreeNode("Grand total")), new DefaultTreeModel(new DefaultMutableTreeNode("Grand total")));
    }

    /**
     * Initialises multiple dimension cube model for specified {@link EntitiesMultipleDimensionCubeData} instance.
     */
    public EntitiesMultipleDimensionCubeModel(final EntitiesMultipleDimensionCubeData<T> data) {
        super(new DefaultTreeModel(new DefaultMutableTreeNode("Grand total")), new DefaultTreeModel(new DefaultMutableTreeNode("Grand total")));
        loadData(data);
    }

    private void loadData(final EntitiesMultipleDimensionCubeData<T> data) {
        this.rowDistributionProperties.addAll(data.getRowDistributionProperties());
        this.columnDistributionProperties.addAll(data.getColumnDistributionProperties());
        this.aggregationProperties.addAll(data.getAggregationProperties());

        final Map<String, String> columnNames = data.getColumnNames();
        final Map<String, String> columnTips = data.getColumnToolTips();
        final Map<String, Class<?>> columnClass = data.getColumnClass();
        for (final String property : aggregationProperties) {
            addValueColumn(property)//
            .setColumnName(property, columnNames.get(property))//
            .setColumnToolTip(property, columnTips.get(property))//
            .setColumnClass(property, columnClass.get(property));
        }

        final Map<List<String>, List<T>> loadedData = data.getData();
        addData(getRowRoot(), getColumnRoot(), loadedData.get(new ArrayList<>()).get(0));
        buildTreeModel(true, loadedData);
        buildTreeModel(false, loadedData);

        final List<String> rowDistribution = new ArrayList<>();
        for (final String rowProperty : rowDistributionProperties) {
            rowDistribution.add(rowProperty);
            final List<String> columnDistribution = new ArrayList<>();
            for (final String columnProperty : columnDistributionProperties) {
                columnDistribution.add(columnProperty);
                final List<String> group = new ArrayList<>(rowDistribution);
                group.addAll(columnDistribution);
                final List<T> dataList = loadedData.get(group);
                loadDataList(rowDistribution, columnDistribution, dataList);
            }
        }
        rowNodes.clear();
        columnNodes.clear();
    }

    private void loadDataList(final List<String> rowDistribution, final List<String> columnDistribution, final List<T> dataList) {
        for (final T entry : dataList) {
            final DefaultMutableTreeNode rowNode = findNodePair(true, rowDistribution, entry).getKey();
            final DefaultMutableTreeNode columnNode = findNodePair(false, columnDistribution, entry).getKey();
            addData(rowNode, columnNode, entry);
        }
    }

    private void buildTreeModel(final boolean isRow, final Map<List<String>, List<T>> data) {
        final List<String> tempDistribution = new ArrayList<>();
        for (final String property : (isRow ? rowDistributionProperties : columnDistributionProperties)) {
            tempDistribution.add(property);
            final List<T> listToLoad = data.get(tempDistribution);
            for (final T entry : listToLoad) {
                addEntry(isRow, tempDistribution, entry);
            }
        }
    }

    private void addEntry(final boolean isRow, final List<String> distribution, final T entry) {
        final List<String> tempDistribution = distribution.subList(0, distribution.size() - 1);
        final Pair<DefaultMutableTreeNode, Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>>> nodePair = findNodePair(isRow, tempDistribution, entry);
        final Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>> currentLevel = nodePair.getValue();
        final DefaultMutableTreeNode currentNode = nodePair.getKey();
        final Object currentKey = entry.get(distribution.get(distribution.size() - 1));
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode(currentKey);
        final Pair<DefaultMutableTreeNode, Map<?, ?>> childPair = new Pair<DefaultMutableTreeNode, Map<?, ?>>(child, new HashMap<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>>());
        currentLevel.put(currentKey, childPair);
        currentNode.add(child);
        addData(isRow ? child : getRowRoot(), isRow ? getColumnRoot() : child, entry);
    }

    @SuppressWarnings("unchecked")
    private Pair<DefaultMutableTreeNode, Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>>> findNodePair(final boolean isRow, final List<String> distribution, final T entry) {
        Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>> currentLevel = isRow ? rowNodes : columnNodes;
        DefaultMutableTreeNode currentNode = isRow ? getRowRoot() : getColumnRoot();
        for (final String property : distribution) {
            final Object key = entry.get(property);
            final Pair<DefaultMutableTreeNode, Map<?, ?>> nodePair = currentLevel.get(key);
            if (nodePair == null) {
                throw new IllegalStateException("The node with " + key + " wasn't found!");
            }
            currentLevel = (Map<Object, Pair<DefaultMutableTreeNode, Map<?, ?>>>) nodePair.getValue();
            currentNode = nodePair.getKey();
        }
        return new Pair<>(currentNode, currentLevel);
    }

    private void addData(final DefaultMutableTreeNode currentRowNode, final DefaultMutableTreeNode currentColumnNode, final T data) {
        for (int valueIndex = 0; valueIndex < aggregationProperties.size(); valueIndex++) {
            setValueAt(currentRowNode, currentColumnNode, valueIndex, data.get(aggregationProperties.get(valueIndex)));
        }
    }

    /**
     * Returns the root for the column tree model.
     * 
     * @return
     */
    public DefaultMutableTreeNode getColumnRoot() {
        return (DefaultMutableTreeNode) getColumnModel().getRoot();
    }

    /**
     * Returns the root for the row tree model.
     * 
     * @return
     */
    public DefaultMutableTreeNode getRowRoot() {
        return (DefaultMutableTreeNode) getRowModel().getRoot();
    }

    /**
     * Returns the list of property names those where used for constructing row tree model.
     * 
     * @return
     */
    public List<String> getRowDistributionProperties() {
        return new ArrayList<>(rowDistributionProperties);
    }

    /**
     * Returns the list of property names those where used for constructing column tree model.
     * 
     * @return
     */
    public List<String> getColumnDistributionProperties() {
        return new ArrayList<>(columnDistributionProperties);
    }

    /**
     * Returns the list of properties which were aggregated.
     * 
     * @return
     */
    public List<String> getAggregationProperties() {
        return new ArrayList<>(aggregationProperties);
    }
}
