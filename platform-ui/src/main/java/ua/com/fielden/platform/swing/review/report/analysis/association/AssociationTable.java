package ua.com.fielden.platform.swing.review.report.analysis.association;

import javax.swing.JTable;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Table the view the association between {@link User} and {@link UserRole}
 * 
 * @author TG Team
 * 
 */
public class AssociationTable extends JTable {

    private static final long serialVersionUID = -4844803648466115846L;

    //    /**
    //     * Creates new instance of the {@link AssociationTable} with specified {@link AssociationTableModel}
    //     *
    //     * @param associationTableModel
    //     */
    //    public AssociationTable(final AssociationTableModel associationTableModel) {
    //	super(associationTableModel);
    //
    //	addMouseListener(createCallMasterAction());
    //
    //	setDefaultRenderer(Boolean.class, new TableCellRenderer() {
    //
    //	    private final TableCellRenderer stringCellRenderer = new DefaultTableCellRenderer(),//
    //		    booleanCellRenderer = getDefaultRenderer(Boolean.class);
    //
    //	    @Override
    //	    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    //		final Component stringComponent = stringCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    //		final JCheckBox boolComponent = (JCheckBox) booleanCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    //		final Color backgroundColor = stringComponent.getBackground();
    //		boolComponent.setBackground(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()));
    //		boolComponent.setOpaque(true);
    //		return boolComponent;
    //	    }
    //
    //	});
    //
    //	for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
    //
    //	    if (!isUserColumn(columnIndex)) {
    //		setVerticalHeaderFor(getColumnModel().getColumn(columnIndex));
    //		getColumnModel().getColumn(columnIndex).setPreferredWidth(0);
    //	    }
    //	}
    //
    //	final MouseDefaultHeaderHandler mouseHandler = new MouseDefaultHeaderHandler();
    //	getTableHeader().addMouseMotionListener(mouseHandler);
    //	getTableHeader().addMouseListener(mouseHandler);
    //	getTableHeader().setReorderingAllowed(false);
    //	addToolTipSuportForTableHeader();
    //
    //	setShowGrid(true);
    //	setGridColor(new Color(214, 217, 223));
    //    }
    //
    //    private MouseListener createCallMasterAction() {
    //	return new MouseAdapter() {
    //
    //	    @Override
    //	    public void mouseClicked(final MouseEvent e) {
    //		super.mouseClicked(e);
    //		if (e.getClickCount() == 2) {
    //		    //TODO fix errors here.
    //		    //final AssociationClickEvent event = new AssociationClickEvent(AssociationTable.class, rowClicked, columnClicked, valueInIntersection);
    //		    for (final IAssociationDoubleClickListener listener : listenerList.getListeners(IAssociationDoubleClickListener.class)) {
    //			//listener.cellDoubleClicked(event);
    //		    }
    //		}
    //	    }
    //	};
    //    }
    //
    //    public void addAssociationDoubleClickListener(final IAssociationDoubleClickListener listener) {
    //	listenerList.add(IAssociationDoubleClickListener.class, listener);
    //    }
    //
    //    public void removeAssociationDoubleClickListener(final IAssociationDoubleClickListener listener) {
    //	listenerList.remove(IAssociationDoubleClickListener.class, listener);
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
    //		header.setToolTipText(((AssociationTableModel) getModel()).getColumnHeaderToolTip(convertColumnIndexToModel(vColIndex)));
    //	    }
    //	});
    //    }
    //
    //    /**
    //     * see {@link AssociationTableModel#isUserColumn}. Note that the <code>viewColumnIndex</code> is the view column index (not the index of the column in table model)
    //     *
    //     * @param viewColumnIndex
    //     * @return
    //     */
    //    private boolean isUserColumn(final int viewColumnIndex) {
    //	return ((AssociationTableModel) getModel()).isUserColumn(convertColumnIndexToModel(viewColumnIndex));
    //    }
    //
    //    /**
    //     * set the vertical direction of the column header
    //     *
    //     * @param column
    //     */
    //    private void setVerticalHeaderFor(final TableColumn column) {
    //	column.setHeaderRenderer(new VerticalTableHeaderCellRenderer());
    //    }
    //
    //    @Override
    //    public String getToolTipText(final MouseEvent event) {
    //	final int row = rowAtPoint(event.getPoint());
    //	final int col = columnAtPoint(event.getPoint());
    //	if (row >= 0 && col >= 0) {
    //	    return ((AssociationTableModel) getModel()).getCellToolTip(convertRowIndexToModel(row), convertColumnIndexToModel(col));
    //	}
    //	return super.getToolTipText(event);
    //    }
    //
    //    @Override
    //    public void addColumn(final TableColumn column) {
    //	super.addColumn(column);
    //	if (!(column.getHeaderRenderer() instanceof VerticalTableHeaderCellRenderer)) {
    //	    if (!isUserColumn(column.getModelIndex())) {
    //		setVerticalHeaderFor(column);
    //	    }
    //	}
    //    }
}
