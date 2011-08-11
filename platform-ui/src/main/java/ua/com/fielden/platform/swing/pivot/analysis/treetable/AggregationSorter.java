package ua.com.fielden.platform.swing.pivot.analysis.treetable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.SortOrder;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.swing.checkboxlist.SortObject;
import ua.com.fielden.platform.swing.pivot.analysis.PivotAnalysisReportModel;
import ua.com.fielden.platform.utils.Pair;

public class AggregationSorter<T extends AbstractEntity, DAO extends IEntityDao<T>> implements Comparator<MutableTreeTableNode> {

    private final PivotAnalysisReportModel<T, DAO> reportModel;

    public AggregationSorter(final PivotAnalysisReportModel<T, DAO> reportModel) {
	this.reportModel = reportModel;
    }

    @Override
    public int compare(final MutableTreeTableNode o1, final MutableTreeTableNode o2) {
	final List<SortObject<IAggregatedProperty>> sortObjects = reportModel.getSortingAggregations();
	if (sortObjects == null || sortObjects.isEmpty()) {
	    return defaultCompare(o1, o2);
	}
	final List<Pair<Integer, SortOrder>> sortOrders = new ArrayList<Pair<Integer, SortOrder>>();
	final List<IAggregatedProperty> columns = reportModel.getSelectedAggregationProperties();
	for (final SortObject<IAggregatedProperty> aggreagationProperty : sortObjects) {
	    final int sortOrder = getColumn(aggreagationProperty.getSortObject(), columns);
	    if (sortOrder >= 0) {
		sortOrders.add(new Pair<Integer, SortOrder>(Integer.valueOf(sortOrder), aggreagationProperty.getSortOrder()));
	    }
	}
	if (sortOrders.isEmpty()) {
	    return defaultCompare(o1, o2);
	}
	for (final Pair<Integer, SortOrder> sortingParam : sortOrders) {
	    final Comparable value1 = (Comparable) o1.getValueAt(sortingParam.getKey().intValue() + 1);
	    final Comparable value2 = (Comparable) o2.getValueAt(sortingParam.getKey().intValue() + 1);
	    int result = 0;
	    if (value1 == null) {
		if (value2 != null) {
		    return -1;
		}
	    } else {
		if (value2 == null) {
		    return 1;
		} else {
		    result = compareValues(value1, value2, sortingParam.getValue());
		}
	    }
	    if (result != 0) {
		return result;
	    }
	}
	return defaultCompare(o1, o2);
    }

    private int compareValues(final Comparable value1, final Comparable value2, final SortOrder sortingParam) {
	final int sortMultiplier = sortingParam == SortOrder.ASCENDING ? 1 : (sortingParam == SortOrder.DESCENDING ? -1 : 0);
	return value1.compareTo(value2) * sortMultiplier;
    }

    private int defaultCompare(final MutableTreeTableNode o1, final MutableTreeTableNode o2) {
	if (o1.getUserObject().equals(PivotTreeTableNode.NULL_USER_OBJECT)) {
	    if (o2.getUserObject().equals(PivotTreeTableNode.NULL_USER_OBJECT)) {
		return 0;
	    } else {
		return -1;
	    }
	} else {
	    if (o2.getUserObject().equals(PivotTreeTableNode.NULL_USER_OBJECT)) {
		return 1;
	    } else {
		return o1.getUserObject().toString().compareTo(o2.getUserObject().toString());
	    }
	}

    }

    private int getColumn(final IAggregatedProperty aggregationProperty, final List<IAggregatedProperty> columns) {
	for (int index = 0; index < columns.size(); index++) {
	    final IAggregatedProperty anotherAggregation = columns.get(index);
	    if (anotherAggregation.equals(aggregationProperty)) {
		return index;
	    }
	}
	return -1;
    }

}
