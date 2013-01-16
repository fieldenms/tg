package ua.com.fielden.platform.swing.categorychart;

import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;

/**
 * Custom drug to support. Supports dragging in to the list.
 *
 * @author TG Team
 *
 */
public class AnalysisListDragToSupport<T extends AbstractEntity<?>> implements DragToSupport {

    private final JList<String> list;

    protected final ITickManager tickManager;

    protected final Class<T> root;

    /**
     * Initialises this {@link AnalysisListDragToSupport} with the "drag into" list, and {@link IAbstractAnalysisDomainTreeManager} instance.
     *
     * @param list
     */
    public AnalysisListDragToSupport(final JList<String> list, final Class<T> root, final ITickManager tickManager) {
	this.list = list;
	this.root = root;
	this.tickManager = tickManager;
    }

    @Override
    public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
	return draggedFrom == list;
    }

    @Override
    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
	final int fromIndex = ((DefaultListModel<String>)list.getModel()).indexOf(what);
	final int toIndex = list.locationToIndex(point);
	if (toIndex < 0) {
	    return false;
	}
	final int increment = fromIndex > toIndex ? 0 : 1;
	if(toIndex < list.getModel().getSize() - 1){
	    tickManager.move(root, what.toString(), list.getModel().getElementAt(toIndex + increment).toString());
	}else if(toIndex == list.getModel().getSize() - 1){
	    tickManager.moveToTheEnd(root, what.toString());
	}
	((DefaultListModel<String>) list.getModel()).removeElement(what);
	((DefaultListModel<String>) list.getModel()).add(toIndex, what.toString());
	list.setSelectedIndex(toIndex);
	return true;
    }

    protected JList<String> getList() {
	return list;
    }
}
