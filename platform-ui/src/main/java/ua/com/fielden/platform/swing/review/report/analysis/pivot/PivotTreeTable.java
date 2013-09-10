package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.TreeTableNode;

import ua.com.fielden.platform.swing.treetable.FilterableTreeTable;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;

//TODO implement
public class PivotTreeTable extends FilterableTreeTable {

    //    public PivotTreeTable(final FilterableTreeTableModel treeTableModel) {
    //	super(treeTableModel, false);
    //    }

    private static final long serialVersionUID = -731155079399567971L;

    public PivotTreeTable(final FilterableTreeTableModel treeTableModel) {
	super(treeTableModel, true);

	getTableHeader().setReorderingAllowed(false);
	addToolTipSuportForTableHeader();
	setShowGrid(true, true);
	setGridColor(new Color(214, 217, 223));
	setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
	setColumnFactory(createPivotColumnFactory());

	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
	getColumnModel().addColumnModelListener(createColumnModelListener(pivotModel, createColumnWidthChangeListener(pivotModel)));

	pivotModel.addTableHeaderChangedListener(new PivotTableHeaderChanged() {

	    @Override
	    public void tableHeaderChanged(final PivotTableHeaderChangedEvent event) {
		refreshPivotTable();
	    }

	    @Override
	    public void columnOrderChanged(final PivotColumnOrderChangedEvent event) {
		//This is stub implementation.
	    }
	});

	pivotModel.addSorterChangeListener(new PivotTableSorterListener() {

	    @Override
	    public void sorterChanged(final PivotSorterChangeEvent event) {
		reloadWithoutCollapsing();
	    }
	});

	getColumnModel().addColumnModelListener(new TableColumnModelListener() {

	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
		final TableColumn column = getColumnModel().getColumn(e.getToIndex());
		final Class<?>[] columnTypes = pivotModel.getColumnTypes(e.getToIndex());
		column.setCellRenderer(createCellRenderer(columnTypes));
	    }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {

	    }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {

	    }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {

	    }

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {

	    }

	});
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        final int row = rowAtPoint(event.getPoint());
        final int col = columnAtPoint(event.getPoint());
        if (row >= 0 && col >= 0) {
            final PivotTreeTableNode node = (PivotTreeTableNode) getPathForRow(row).getLastPathComponent();
            return node.getTooltipAt(col);
        }
        return super.getToolTipText(event);
    }

    /**
     * Returns the path for row at the point specified with x and y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    public TreePath getRowPathForLocation(final int x, final int y){
	return getPathForLocation(x, y);
    }

    /**
     * Returns the path for column at the point specified with x and y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    public TreePath getColumnPathForLocation(final int x, final int y){
	final int column = columnAtPoint(new Point(x, y));
	if(column < 0){
	    return null;
	} else {
	    final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
	    return pivotModel.getPathForColumn(column);
	}
    }

    /**
     * Creates the table column listener for the tree table model that updates the column's width.
     *
     * @param columnWidthChangeListener
     * @return
     */
    private TableColumnModelListener createColumnModelListener(final PivotTreeTableModel pivotTableModel, final PropertyChangeListener columnWidthChangeListener) {
	return new TableColumnModelListener() {

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {}

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {}

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {}

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {}

	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
		final int columnIndex = e.getToIndex();
		final TableColumn column = getColumnModel().getColumn(columnIndex);
		final int width = pivotTableModel.getColumnWidth(columnIndex);
		if(width > 0){
		    column.setPreferredWidth(width);
		}
		column.addPropertyChangeListener(columnWidthChangeListener);
	    }
	};
    }

    /**
     * Creates the column's width property change listener. Updates model property's width.
     *
     * @param pivotTable
     * @return
     */
    private PropertyChangeListener createColumnWidthChangeListener(final PivotTreeTableModel pivotTableModel) {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("width")) {
		    final int columnIndex = getColumnModel().getColumnIndex(((TableColumn)evt.getSource()).getIdentifier());
		    pivotTableModel.setColumnWidth(columnIndex, ((Integer) evt.getNewValue()).intValue());
		}
	    }
	};
    }

    /**
     *
     *
     * @param columnTypes
     * @return
     */
    private TableCellRenderer createCellRenderer(final Class<?>[] columnTypes) {
	return new PivotTableCellRenderer(1, new Color(214, 217, 223), columnTypes);
    }


        /**
         * Returns the column factory for this {@link PivotTreeTable} instance.
         *
         * @return
         */
        private ColumnFactory createPivotColumnFactory() {
    	return new ColumnFactory() {
    	    @Override
    	    public void configureColumnWidths(final JXTable table, final TableColumnExt columnExt) {
    		final int index = table.getColumnModel().getColumnIndex(columnExt.getIdentifier());
    		final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
    		final int width = pivotModel.getColumnWidth(index);
    		if (width == 0) {
    		    super.configureColumnWidths(table, columnExt);
    		} else {
    		    columnExt.setPreferredWidth(width);
    		}
    	    }
    	};
        }

    /**
     * Adds the tool tips to the table headers.
     */
    private void addToolTipSuportForTableHeader() {
	getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
	    @Override
	    public void mouseMoved(final MouseEvent e) {
		final int vColIndex = columnAtPoint(e.getPoint());
		final JTableHeader header = getTableHeader();

		if (vColIndex >= 0) {
		    final PivotTreeTableModel treeTableModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
		    header.setToolTipText(treeTableModel.getColumnTooltipAt(convertColumnIndexToModel(vColIndex)));

		}
	    }
	});
    }

    /**
     * Refreshes the pivot tree table.
     * @param treeTable
     */
    private void refreshPivotTable(){
	final TreePath selectedPath = getPathForRow(getSelectedRow());
	((AbstractTableModel) getModel()).fireTableStructureChanged();
	getSelectionModel().setSelectionInterval(0, getRowForPath(selectedPath));
    }

    private void reloadWithoutCollapsing() {
	final FilterableTreeTableModel filterableModel = getFilterableModel();
	final TreeTableNode rootNode = filterableModel.getOriginModel().getRoot();
	final TreePath selectedPath = getPathForRow(getSelectedRow());
	if (rootNode != null) {
	    final Enumeration<?> expandedPaths = getExpandedDescendants(new TreePath(rootNode));
	    filterableModel.reload();
	    while (expandedPaths != null && expandedPaths.hasMoreElements()) {
		final TreePath path = (TreePath) expandedPaths.nextElement();
		expandPath(path);
	    }
	}
	scrollPathToVisible(selectedPath);
	getSelectionModel().setSelectionInterval(0, getRowForPath(selectedPath));
    }
}
