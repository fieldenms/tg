package ua.com.fielden.platform.example.swing.draggablelist;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JList;

import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;

public class DragFromSupportImplementation implements DragFromSupport {

    private final JList list;

    public DragFromSupportImplementation(final JList list) {
	this.list = list;
    }

    @Override
    public Object getObject4DragAt(final Point point) {
	return list.getModel().getElementAt(list.locationToIndex(point));
    }

    @Override
    public void dragNDropDone(final Object object, final JComponent dropTo, final int action) {
	if (dropTo == list) {
	    //((DefaultListModel) list.getModel()).removeElement(object);
	}
    }

}
