package ua.com.fielden.platform.swing.categorychart;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JList;

import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;

/**
 * The {@link DragFromSupport} that supports dragging from the specified list.
 *
 * @author TG Team
 *
 */
public class AnalysisListDragFromSupport implements DragFromSupport {

    private final JList list;

    /**
     * Initialises this {@link AnalysisListDragFromSupport} with the "drag from" list.
     *
     * @param list
     */
    public AnalysisListDragFromSupport(final JList list) {
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
