package ua.com.fielden.platform.swing.sortabletable;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.verticallabel.MouseDefaultHeaderHandler;

public class SorterHandler<T extends AbstractEntity> extends MouseAdapter {

    private PropertyTableModelRowSorter<T> rowSorter;
    private JTable table;
    private MouseDefaultHeaderHandler mouseHandler;

    public SorterHandler() {

    }

    public void install(final JTable table, final PropertyTableModelRowSorter<T> rowSorter) {
	if (this.rowSorter != null) {
	    uninstall();
	}
	this.rowSorter = rowSorter;
	this.table = table;
	table.setRowSorter(rowSorter);
	table.getTableHeader().addMouseListener(this);
	for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
	    table.getColumnModel().getColumn(columnIndex).setHeaderRenderer(new SorterTableHeaderCellRenderer());
	}
	mouseHandler = new MouseDefaultHeaderHandler();
	table.getTableHeader().addMouseMotionListener(mouseHandler);
	table.getTableHeader().addMouseListener(mouseHandler);
    }

    public void uninstall() {
	this.rowSorter = null;

	getTable().setRowSorter(null);
	getTable().getTableHeader().removeMouseListener(this);
	for (int columnIndex = 0; columnIndex < getTable().getColumnCount(); columnIndex++) {
	    getTable().getColumnModel().getColumn(columnIndex).setHeaderRenderer(null);
	}
	getTable().getTableHeader().removeMouseListener(mouseHandler);
	getTable().getTableHeader().removeMouseMotionListener(mouseHandler);
	mouseHandler = null;

	this.table = null;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
	final TableColumnModel columnModel = getTable().getColumnModel();
	final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
	final int column = getTable().convertColumnIndexToModel(viewColumn);
	if (e.getClickCount() == 1 && column != -1 && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
	    rowSorter.toggleSortOrder(column, false);
	}

    }

    public PropertyTableModelRowSorter<T> getRowSorter() {
	return rowSorter;
    }

    protected JTable getTable() {
	return table;
    }
}
