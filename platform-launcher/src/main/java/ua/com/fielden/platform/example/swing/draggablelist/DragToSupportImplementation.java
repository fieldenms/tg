package ua.com.fielden.platform.example.swing.draggablelist;

import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;

import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;

public class DragToSupportImplementation implements DragToSupport {

    private final JList list;

    public DragToSupportImplementation(final JList list) {
	this.list = list;
    }

    @Override
    public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
	return draggedFrom == list;
    }

    @Override
    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
	final int index = list.locationToIndex(point);
	if (index < 0) {
	    return false;
	}
	((DefaultListModel) list.getModel()).removeElement(what);
	((DefaultListModel) list.getModel()).add(index, what);
	return true;
    }

}
