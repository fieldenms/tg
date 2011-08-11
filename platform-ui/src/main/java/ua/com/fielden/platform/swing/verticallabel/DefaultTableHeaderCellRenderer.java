package ua.com.fielden.platform.swing.verticallabel;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.sun.java.swing.Painter;

/**
 * A default cell renderer for a JTableHeader.
 * <P>
 * Extends {@link DefaultTableCellRenderer}.
 * <P>
 * 
 * To apply any desired customization, DefaultTableHeaderCellRenderer may be suitably extended.
 * 
 * @author oleh
 */
public class DefaultTableHeaderCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -5035173249726516808L;

    private boolean mouseOver;

    // painters for table header cell. Added to support Nimbus L&F
    private final Painter headerEnable;
    private final Painter headerMouseOver;

    /**
     * Constructs a <code>DefaultTableHeaderCellRenderer</code>.
     * <P>
     * The horizontal alignment and text position are set as appropriate to a table header cell, and the renderer is set to be non-opaque.
     */
    public DefaultTableHeaderCellRenderer() {
	setHorizontalAlignment(CENTER);
	setHorizontalTextPosition(LEFT);
	setOpaque(false);

	headerEnable = (Painter) UIManager.get("TableHeader:" + '"' + "TableHeader.renderer" + '"' + "[Enabled].backgroundPainter");
	headerMouseOver = (Painter) UIManager.get("TableHeader:" + '"' + "TableHeader.renderer" + '"' + "[MouseOver].backgroundPainter");

    }

    /**
     * Returns the default table header cell renderer component.
     * <P>
     * The icon is set as appropriate for the header cell of a sorted or unsorted column, and the border appropriate to a table header cell is applied.
     * <P>
     * Subclasses may override this method to provide custom content or formatting.
     * 
     * @param table
     *            the <code>JTable</code>.
     * @param value
     *            the value to assign to the header cell
     * @param isSelected
     *            This parameter is ignored.
     * @param hasFocus
     *            This parameter is ignored.
     * @param row
     *            This parameter is ignored.
     * @param column
     *            the column of the header cell to render
     * @return the default table header cell renderer
     * 
     * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	setIcon(getIcon(table, column));
	setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	return this;
    }

    @Override
    public void paint(final Graphics g) {
	if (isMouseOver()) {
	    if (headerMouseOver != null) {
		headerMouseOver.paint((Graphics2D) g, null, getWidth(), getHeight());
	    }
	} else {
	    if (headerEnable != null) {
		headerEnable.paint((Graphics2D) g, null, getWidth(), getHeight());
	    }
	}
	super.paint(g);
    }

    /**
     * Overridden to return an icon suitable to a sorted column, or null if the column is unsorted.
     * 
     * @param table
     *            the <code>JTable</code>.
     * @param column
     *            the colummn index.
     * @return the sort icon, or null if the column is unsorted.
     */
    protected Icon getIcon(final JTable table, final int column) {
	final SortKey sortKey = getSortKey(table, column);
	if (sortKey != null && sortKey.getColumn() == column) {
	    final SortOrder sortOrder = sortKey.getSortOrder();
	    switch (sortOrder) {
	    case ASCENDING:
		return UIManager.getIcon("Table.ascendingSortIcon");
	    case DESCENDING:
		return UIManager.getIcon("Table.descendingSortIcon");
	    }
	}
	return null;
    }

    protected SortKey getSortKey(final JTable table, final int column) {
	final RowSorter rowSorter = table.getRowSorter();
	if (rowSorter == null) {
	    return null;
	}

	final List sortedColumns = rowSorter.getSortKeys();
	if (sortedColumns.size() > 0) {
	    return (SortKey) sortedColumns.get(0);
	}
	return null;
    }

    /**
     * Set the mouse over property. This property is used during painting the cell renderer component.
     * 
     * @param mouseOver
     */
    public void setMouseOver(final boolean mouseOver) {
	this.mouseOver = mouseOver;
    }

    /**
     * Returns the mouse over property. See {@link #setMouseOver(boolean)} for more information about that property.
     * 
     * @return
     */
    public boolean isMouseOver() {
	return mouseOver;
    }

}
