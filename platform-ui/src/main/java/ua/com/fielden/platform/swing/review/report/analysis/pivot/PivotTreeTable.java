package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import ua.com.fielden.platform.swing.treetable.FilterableTreeTable;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;

//TODO implement
public class PivotTreeTable extends FilterableTreeTable {

    public PivotTreeTable(final FilterableTreeTableModel treeTableModel) {
	super(treeTableModel, false);
    }

    private static final long serialVersionUID = -731155079399567971L;

//    public PivotTreeTable(final FilterableTreeTableModel treeTableModel) {
//	super(treeTableModel, false);
//
//	getTableHeader().setReorderingAllowed(false);
//	addToolTipSuportForTableHeader();
//	setShowGrid(true, true);
//	setGridColor(new Color(214, 217, 223));
//	setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//	setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
//	setColumnFactory(createPivotColumnFactory());
//
//	getColumnModel().addColumnModelListener(new TableColumnModelListener() {
//
//	    @Override
//	    public void columnAdded(final TableColumnModelEvent e) {
//		final TableColumn column = getColumnModel().getColumn(e.getToIndex());
//		final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//		final Class<?> columnClass = pivotModel.getColumnClass(e.getToIndex());
//		column.setCellRenderer(createCellRenderer(columnClass));
//		column.addPropertyChangeListener(new PropertyChangeListener() {
//
//		    @Override
//		    public void propertyChange(final PropertyChangeEvent evt) {
//			if (evt.getPropertyName().equals("width")) {
//			    final int index = getColumnModel().getColumnIndex(column.getIdentifier());
//			    pivotModel.setColumnWidthAt(index, ((Integer) evt.getNewValue()).intValue());
//			}
//		    }
//		});
//	    }
//
//	    @Override
//	    public void columnMarginChanged(final ChangeEvent e) {
//
//	    }
//
//	    @Override
//	    public void columnMoved(final TableColumnModelEvent e) {
//
//	    }
//
//	    @Override
//	    public void columnRemoved(final TableColumnModelEvent e) {
//
//	    }
//
//	    @Override
//	    public void columnSelectionChanged(final ListSelectionEvent e) {
//
//	    }
//
//	});
//    }
//
//    private TableCellRenderer createCellRenderer(final Class<?> columnClass) {
//	return new PivotTableCellRenderer(columnClass);
//    }
//
//    private ColumnFactory createPivotColumnFactory() {
//	return new ColumnFactory() {
//	    @Override
//	    public void configureColumnWidths(final JXTable table, final TableColumnExt columnExt) {
//		final int index = table.getColumnModel().getColumnIndex(columnExt.getIdentifier());
//		final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//		final int width = pivotModel.getColumnWidthAt(index);
//		if (width == 0) {
//		    super.configureColumnWidths(table, columnExt);
//		} else {
//		    columnExt.setPreferredWidth(width);
//		}
//	    }
//	};
//    }
//
//    // adds the tool tips to the table headers
//    private void addToolTipSuportForTableHeader() {
//	getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
//	    @Override
//	    public void mouseMoved(final MouseEvent e) {
//		final int vColIndex = columnAtPoint(e.getPoint());
//		final JTableHeader header = getTableHeader();
//
//		if (vColIndex >= 0) {
//		    final PivotTreeTableModel treeTableModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//		    header.setToolTipText(treeTableModel.getColumnToolTip(convertColumnIndexToModel(vColIndex)));
//
//		}
//	    }
//	});
//    }
//
//    @Override
//    public String getToolTipText(final MouseEvent event) {
//	final int row = rowAtPoint(event.getPoint());
//	final int col = columnAtPoint(event.getPoint());
//	if (row >= 0 && col >= 0) {
//	    final PivotTreeTableNode node = (PivotTreeTableNode) getPathForRow(row).getLastPathComponent();
//	    return node.getTooltipAt(col);
//	}
//	return super.getToolTipText(event);
//    }
//
//    public void addGroupParameter(final IDistributedProperty group, final int index) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.addGroupParameter(group, index);
//	refreshTreeTable();
//    }
//
//    public IDistributedProperty removeGroupParameter(final int index) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	final IDistributedProperty removedGroup = pivotModel.removeGroupParameter(index);
//	refreshTreeTable();
//	return removedGroup;
//    }
//
//    public boolean removeGroupParameter(final IDistributedProperty group) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	final boolean result = pivotModel.removeGroupParameter(group);
//	refreshTreeTable();
//	return result;
//
//    }
//
//    public void addTotalColumn(final Pair<IAggregatedProperty, Integer> column, final int index) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.addColumn(column, index);
//	refreshTreeTable();
//    }
//
//    public Pair<IAggregatedProperty, Integer> removeTotalColumn(final int index) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	final Pair<IAggregatedProperty, Integer> removedColumn = pivotModel.removeTotalColumn(index);
//	refreshTreeTable();
//	return removedColumn;
//    }
//
//    public boolean removeTotalColumn(final IAggregatedProperty column) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	final boolean result = pivotModel.removeTotalColumn(column);
//	refreshTreeTable();
//	return result;
//    }
//
//    public void setTreeTableSorter(final Comparator<MutableTreeTableNode> treeTableSorter) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.setTreeTableSorter(treeTableSorter);
//	reloadWithoutCollapsing();
//    }
//
//    public void toggleSorter() {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.toggleSorter();
//	reloadWithoutCollapsing();
//    }
//
//    private void reloadWithoutCollapsing() {
//	final FilterableTreeTableModel filterableModel = (FilterableTreeTableModel) getTreeTableModel();
//	final TreeTableNode rootNode = filterableModel.getOriginModel().getRoot();
//	final TreePath selectedPath = getPathForRow(getSelectedRow());
//	if(rootNode!=null){
//	    final Enumeration<?> expandedPaths = getExpandedDescendants(new TreePath(rootNode));
//	    filterableModel.reload();
//	    while(expandedPaths != null && expandedPaths.hasMoreElements()){
//		final TreePath path = (TreePath)expandedPaths.nextElement();
//		expandPath(path);
//	    }
//	}
//	scrollPathToVisible(selectedPath);
//	getSelectionModel().setSelectionInterval(0, getRowForPath(selectedPath));
//    }
//
//    public void loadData(final GroupItem root) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.loadData(root);
//    }
//
//    private void refreshTreeTable() {
//	if (getModel() instanceof AbstractTableModel) {
//	    final TreePath selectedPath = getPathForRow(getSelectedRow());
//	    ((AbstractTableModel) getModel()).fireTableStructureChanged();
//	    //scrollPathToVisible(selectedPath);
//	    getSelectionModel().setSelectionInterval(0, getRowForPath(selectedPath));
//	}
//    }
//
//    public void swapGroupParameter(final int oldIndex, final int newIndex) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.swapGroupParameter(oldIndex, newIndex);
//	refreshTreeTable();
//    }
//
//    public void swapTotalParameter(final int oldIndex, final int newIndex) {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	pivotModel.swapTotalParameter(oldIndex, newIndex);
//	getColumnModel().moveColumn(oldIndex + 1, newIndex + 1);
//	refreshTreeTable();
//    }
//
//    public List<Pair<IAggregatedProperty, Integer>> getAggregationColumnsWidth() {
//	final List<Pair<IAggregatedProperty, Integer>> pivotColumns = new ArrayList<Pair<IAggregatedProperty, Integer>>();
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	for (int column = 1; column < getColumnCount(); column++) {
//	    pivotColumns.add(pivotModel.getTotalColumnAt(column - 1));
//	}
//	return pivotColumns;
//    }
//
//    public int getHierarchicalColumnWidth() {
//	final PivotTreeTableModel pivotModel = (PivotTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
//	return pivotModel.getColumnWidthAt(getHierarchicalColumn());
//    }

}
