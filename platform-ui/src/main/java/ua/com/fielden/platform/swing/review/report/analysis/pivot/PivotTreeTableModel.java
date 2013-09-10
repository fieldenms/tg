package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.tree.TreePath;

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

    /**
     * Set the column width for the column specified with column index.
     *
     * @param column
     * @param width
     */
    abstract void setColumnWidth(int column, int width);

    abstract String getColumnTooltipAt(int column);

    abstract List<String> rowCategoryProperties();

    abstract List<String> aggregatedProperties();

    abstract List<String> columnCategoryProperties();

    abstract TreePath getPathForColumn(int column);

    abstract Class<?>[] getColumnTypes(int column);

    public void addPivotDataLoadedListener(final PivotDataLoadedListener listener) {
	listeners.add(PivotDataLoadedListener.class, listener);
    }

    public void removePivotDataLoadedListener(final PivotDataLoadedListener listener) {
	listeners.remove(PivotDataLoadedListener.class, listener);
    }

    public void addTableHeaderChangedListener(final PivotTableHeaderChanged listener){
	listeners.add(PivotTableHeaderChanged.class, listener);
    }

    public void removePivotTableHeaderChangedListener(final PivotTableHeaderChanged listener){
	listeners.remove(PivotTableHeaderChanged.class, listener);
    }

    public void addPivotHierarchyChangedListener(final PivotHierarchyChangedListener listener){
	listeners.add(PivotHierarchyChangedListener.class, listener);
    }

    public void removePivotHierarchyChangedListener(final PivotHierarchyChangedListener listener){
	listeners.remove(PivotHierarchyChangedListener.class, listener);
    }

    public void addSorterChangeListener(final PivotTableSorterListener listener){
	listeners.add(PivotTableSorterListener.class, listener);
    }

    public void removeSorterChangeListener(final PivotTableSorterListener listener){
	listeners.remove(PivotTableSorterListener.class, listener);
    }

    protected void firePivotDataLoaded(final PivotDataLoadedEvent event){
	for(final PivotDataLoadedListener listener : listeners.getListeners(PivotDataLoadedListener.class)){
	    listener.pivotDataLoaded(event);
	}
    }

    protected void fireTableColumnOrderingChanged(final PivotColumnOrderChangedEvent event){
	for(final PivotTableHeaderChanged listener : listeners.getListeners(PivotTableHeaderChanged.class)){
	    listener.columnOrderChanged(event);
	}
    }

    protected void fireTableHeaderChangedEvent(final PivotTableHeaderChangedEvent event){
	for(final PivotTableHeaderChanged listener : listeners.getListeners(PivotTableHeaderChanged.class)){
	    listener.tableHeaderChanged(event);
	}
    }

    protected void fireTableHierarchyChangedEvent(final PivotHierarchyChangedEvent event){
	for(final PivotHierarchyChangedListener listener : listeners.getListeners(PivotHierarchyChangedListener.class)){
	    listener.pivotHierarchyChanged(event);
	}
    }

    protected void fireSorterChageEvent(final PivotSorterChangeEvent event){
	for(final PivotTableSorterListener listener : listeners.getListeners(PivotTableSorterListener.class)){
	    listener.sorterChanged(event);
	}
    }
}
