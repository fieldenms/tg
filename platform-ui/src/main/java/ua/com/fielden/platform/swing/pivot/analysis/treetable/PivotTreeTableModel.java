package ua.com.fielden.platform.swing.pivot.analysis.treetable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.pivot.analysis.GroupItem;
import ua.com.fielden.platform.swing.pivot.analysis.IPivotDataProvider;
import ua.com.fielden.platform.swing.treetable.DynamicTreeTableModel;
import ua.com.fielden.platform.utils.Pair;

public class PivotTreeTableModel extends DynamicTreeTableModel {

    private final List<IDistributedProperty> groupParameters = new ArrayList<IDistributedProperty>();
    private final List<Pair<IAggregatedProperty, Integer>> totalColumns = new ArrayList<Pair<IAggregatedProperty, Integer>>();
    private int groupColumWidth = 0;

    private final IPivotDataProvider dataProvider;

    private Comparator<MutableTreeTableNode> treeTableSorter = new DefaultSorter();

    public PivotTreeTableModel(final int groupColumnWidth, final IPivotDataProvider dataProvider) {
	this.dataProvider = dataProvider;
	this.groupColumWidth = groupColumnWidth;
    }

    public void loadData(final GroupItem item) {
	final PivotTreeTableNode rootNode = item.createTree("root", this);
	rootNode.sort(treeTableSorter);
	setRoot(rootNode);
    }

    private void reloadData(final GroupItem item, final String alias) {
	item.updateTree((PivotTreeTableNode) getRoot(), alias);
    }

    String getAggregationAliasFor(final int column) {
	return dataProvider.getAliasFor(totalColumns.get(column).getKey());
    }

    @Override
    public int getColumnCount() {
	return totalColumns.size() + (groupParameters.isEmpty() && totalColumns.isEmpty() ? 0 : 1);
    }

    @Override
    public Class<?> getColumnClass(final int column) {
	if (column == 0) {
	    return String.class;
	}
	return dataProvider.getReturnTypeFor(totalColumns.get(column - 1).getKey());
    }

    private static final char RIGHT_ARROW = '\u2192';

    @Override
    public String getColumnName(final int column) {
	if (column == 0) {
	    String name = "";
	    for (final IDistributedProperty columnEntry : groupParameters) {
		name += RIGHT_ARROW + columnEntry.toString();
	    }
	    return name.isEmpty() ? name : name.substring(1);
	}
	final IAggregatedProperty total = totalColumns.get(column - 1).getKey();
	return total.toString();
    }

    public String getColumnToolTip(final int column) {
	if (column == 0) {
	    String name = "";
	    for (final IDistributedProperty columnEntry : groupParameters) {
		name += RIGHT_ARROW + TitlesDescsGetter.removeHtmlTag(columnEntry.getTooltip());
	    }
	    return name.isEmpty() ? name : TitlesDescsGetter.addHtmlTag(name.substring(1));
	}
	final IAggregatedProperty total = totalColumns.get(column - 1).getKey();
	return TitlesDescsGetter.addHtmlTag(TitlesDescsGetter.removeHtmlTag(total.getTooltip()));
    }

    public void addGroupParameter(final IDistributedProperty group, final int index) {
	if (!groupParameters.contains(group)) {
	    groupParameters.add(index, group);
	    loadData(dataProvider.getData());
	}
    }

    public IDistributedProperty removeGroupParameter(final int index) {
	final IDistributedProperty group = groupParameters.remove(index);
	if (group != null) {
	    loadData(dataProvider.getData());
	}
	return group;
    }

    public boolean removeGroupParameter(final IDistributedProperty group) {
	final boolean result = groupParameters.remove(group);
	if (result) {
	    loadData(dataProvider.getData());
	}
	return result;
    }

    public void addColumn(final Pair<IAggregatedProperty, Integer> column, final int index) {
	if (!totalColumns.contains(column)) {
	    totalColumns.add(index, column);
	    reloadData(dataProvider.getData(), getAggregationAliasFor(index));
	}
    }

    public Pair<IAggregatedProperty, Integer> removeTotalColumn(final int index) {
	return totalColumns.remove(index);
    }

    public boolean removeTotalColumn(final IAggregatedProperty column) {
	for (final Pair<IAggregatedProperty, Integer> pair : totalColumns) {
	    if (pair.getKey().equals(column)) {
		return totalColumns.remove(pair);
	    }
	}
	return false;
    }

    public void toggleSorter() {
	if (getRoot() != null) {
	    ((PivotTreeTableNode) getRoot()).sort(treeTableSorter);
	}
    }

    public Comparator<MutableTreeTableNode> getTreeTableSorter() {
	return treeTableSorter;
    }

    public void setTreeTableSorter(final Comparator<MutableTreeTableNode> treeTableSorter) {
	this.treeTableSorter = treeTableSorter;
	toggleSorter();
    }

    public void swapGroupParameter(final int oldIndex, final int newIndex) {
	final IDistributedProperty group = groupParameters.remove(oldIndex);
	if (group != null) {
	    groupParameters.add(newIndex, group);
	    loadData(dataProvider.getData());
	}
    }

    public void swapTotalParameter(final int oldIndex, final int newIndex) {
	final Pair<IAggregatedProperty, Integer> group = totalColumns.remove(oldIndex);
	if (group != null) {
	    totalColumns.add(newIndex, group);
	}
    }

    public Pair<IAggregatedProperty, Integer> getTotalColumnAt(final int column) {
	return totalColumns.get(column);
    }

    public int getColumnWidthAt(final int column) {
	if (column < 0 || column > totalColumns.size()) {
	    return 0;
	}
	if (column == 0) {
	    return groupColumWidth;
	} else {
	    return totalColumns.get(column - 1).getValue().intValue();
	}
    }

    public void setColumnWidthAt(final int column, final int columnWidth) {
	if (column < 0 || column > totalColumns.size()) {
	    return;
	}
	if (column == 0) {
	    groupColumWidth = columnWidth;
	} else {
	    totalColumns.get(column - 1).setValue(Integer.valueOf(columnWidth));
	}
    }

}
