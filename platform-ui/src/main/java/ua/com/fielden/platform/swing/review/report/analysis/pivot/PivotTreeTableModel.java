package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.List;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.swing.treetable.DynamicTreeTableModel;

abstract class PivotTreeTableModel extends DynamicTreeTableModel {

    private final EventListenerList listeners = new EventListenerList();

    /**
     * Returns the width for the specified column.
     *
     * @param column
     * @return
     */
    abstract int getColumnWidth(int column);

    abstract String getColumnTooltipAt(int column);

    abstract List<String> categoryProperties();

    abstract List<String> aggregatedProperties();

    public void addSorterChangeListener(final PivotTableSorterListener listener){
	listeners.add(PivotTableSorterListener.class, listener);
    }

    public void removeSorterChangeListener(final PivotTableSorterListener listener){
	listeners.remove(PivotTableSorterListener.class, listener);
    }

    protected void fireSorterChageEvent(final PivotSorterChangeEvent event){
	for(final PivotTableSorterListener listener : listeners.getListeners(PivotTableSorterListener.class)){
	    listener.sorterChanged(event);
	}
    }
}
