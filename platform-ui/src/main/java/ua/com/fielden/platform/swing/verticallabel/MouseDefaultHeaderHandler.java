package ua.com.fielden.platform.swing.verticallabel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Handles mouse move, mouse entered and mouse exited events in order to support Nimbus L&F
 *
 * @author oleh
 *
 */
public class MouseDefaultHeaderHandler implements MouseMotionListener, MouseListener {

    private int currentColumn;
    private boolean receiveEvents;

    @Override
    public void mouseDragged(final MouseEvent e) {
	mouseMovedOrDragged(e);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
	mouseMovedOrDragged(e);
    }

    /**
     * Handles mouse move or mouse drag events.
     *
     * @param e
     */
    private void mouseMovedOrDragged(final MouseEvent e){
	if (!(e.getSource() instanceof JTableHeader) || !receiveEvents) {
	    return;
	}
	final JTableHeader tableHeader = (JTableHeader) e.getSource();
	final int vColIndex = tableHeader.columnAtPoint(e.getPoint());

	if (vColIndex != currentColumn) {
	    setPropertiesForCellRenderer(tableHeader.getTable(), currentColumn, Boolean.FALSE);
	    setPropertiesForCellRenderer(tableHeader.getTable(), vColIndex, Boolean.TRUE);
	    currentColumn = vColIndex;
	    tableHeader.repaint();
	}
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

    }

    @Override
    public void mouseEntered(final MouseEvent e) {
	receiveEvents = true;
	if (!(e.getSource() instanceof JTableHeader)) {
	    return;
	}
	final JTableHeader tableHeader = (JTableHeader) e.getSource();
	currentColumn = tableHeader.columnAtPoint(e.getPoint());
	if (setPropertiesForCellRenderer(tableHeader.getTable(), currentColumn, Boolean.TRUE)) {
	    tableHeader.repaint();
	}
    }

    @Override
    public void mouseExited(final MouseEvent e) {
	receiveEvents = false;
	setMouseOverCurrentColumn(e, Boolean.FALSE);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    /**
     * Sets the mouseOver property of the VerticalTableHeaderRederer instance to true if the mouseOver parameter is true otherwise it sets that property to false
     *
     * @param table
     * @param columnIndex
     * @param mouseOver
     * @return
     */
    private boolean setPropertiesForCellRenderer(final JTable table, final int columnIndex, final Boolean mouseOver) {
	if (columnIndex >= 0) {
	    final TableCellRenderer headerCellRenderer = table.getColumnModel().getColumn(columnIndex).getHeaderRenderer();
	    if (headerCellRenderer instanceof DefaultTableHeaderCellRenderer) {
		final DefaultTableHeaderCellRenderer headerRenderer = (DefaultTableHeaderCellRenderer) headerCellRenderer;
		if (mouseOver != null) {
		    headerRenderer.setMouseOver(mouseOver);
		    return true;
		}
	    }
	}
	return false;
    }

    @Override
    public void mousePressed(final MouseEvent e) {
    }

    private void setMouseOverCurrentColumn(final MouseEvent e, final Boolean mouseOver) {
	if (!(e.getSource() instanceof JTableHeader)) {
	    return;
	}
	final JTableHeader tableHeader = (JTableHeader) e.getSource();
	if (setPropertiesForCellRenderer(tableHeader.getTable(), currentColumn, mouseOver)) {
	    tableHeader.repaint();
	}
    }
}
