package ua.com.fielden.platform.swing.usertable;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.verticallabel.MouseDefaultHeaderHandler;
import ua.com.fielden.platform.swing.verticallabel.VerticalTableHeaderCellRenderer;

/**
 * Table the view the association between {@link User} and {@link UserRole}
 * 
 * @author TG Team
 * 
 */
public class UserReviewTable extends JTable {

    private static final long serialVersionUID = -4844803648466115846L;

    /**
     * Creates new instance of the {@link UserReviewTable} with specified {@link UserTableModel}
     * 
     * @param userTableModel
     */
    public UserReviewTable(final UserTableModel userTableModel) {
	super(userTableModel);

	setDefaultRenderer(Boolean.class, new TableCellRenderer() {

	    private final TableCellRenderer stringCellRenderer = new DefaultTableCellRenderer(),//
		    booleanCellRenderer = getDefaultRenderer(Boolean.class);

	    @Override
	    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final Component stringComponent = stringCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final JCheckBox boolComponent = (JCheckBox) booleanCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final Color backgroundColor = stringComponent.getBackground();
		boolComponent.setBackground(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()));
		boolComponent.setOpaque(true);
		return boolComponent;
	    }

	});

	for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {

	    if (!isUserColumn(columnIndex)) {
		setVerticalHeaderFor(getColumnModel().getColumn(columnIndex));
		getColumnModel().getColumn(columnIndex).setPreferredWidth(0);
	    }
	}

	final MouseDefaultHeaderHandler mouseHandler = new MouseDefaultHeaderHandler();
	getTableHeader().addMouseMotionListener(mouseHandler);
	getTableHeader().addMouseListener(mouseHandler);
	getTableHeader().setReorderingAllowed(false);
	addToolTipSuportForTableHeader();

	setShowGrid(true);
	setGridColor(new Color(214, 217, 223));
    }

    // adds the tool tips to the table headers
    private void addToolTipSuportForTableHeader() {
	getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
	    @Override
	    public void mouseMoved(final MouseEvent e) {
		final int vColIndex = columnAtPoint(e.getPoint());
		final JTableHeader header = getTableHeader();

		header.setToolTipText(((UserTableModel) getModel()).getColumnHeaderToolTip(convertColumnIndexToModel(vColIndex)));
	    }
	});
    }

    /**
     * see {@link UserTableModel#isUserColumn}. Note that the <code>viewColumnIndex</code> is the view column index (not the index of the column in table model)
     * 
     * @param viewColumnIndex
     * @return
     */
    public boolean isUserColumn(final int viewColumnIndex) {
	return ((UserTableModel) getModel()).isUserColumn(convertColumnIndexToModel(viewColumnIndex));
    }

    /**
     * set the vertical direction of the column header
     * 
     * @param column
     */
    public void setVerticalHeaderFor(final TableColumn column) {
	column.setHeaderRenderer(new VerticalTableHeaderCellRenderer());
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
	final int row = rowAtPoint(event.getPoint());
	final int col = columnAtPoint(event.getPoint());
	if (row >= 0 && col >= 0) {
	    return ((UserTableModel) getModel()).getCellToolTip(convertRowIndexToModel(row), convertColumnIndexToModel(col));
	}
	return super.getToolTipText(event);
    }

    @Override
    public void addColumn(final TableColumn column) {
	super.addColumn(column);
	if (!(column.getHeaderRenderer() instanceof VerticalTableHeaderCellRenderer)) {
	    if (!isUserColumn(column.getModelIndex())) {
		setVerticalHeaderFor(column);
	    }
	}
    }
}
