package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

public class SortableList<E> extends JList<E> {

    private static final long serialVersionUID = 4696705540337797795L;

    private ListSortingModel<E> sortingModel;

    private SorterEventListener<E> defaultSortingListener;

    /**
     * Whether sorting event causes it to be selected, too.
     */
    private boolean selectBySortingEvent = true;

    public SortableList(final DefaultListModel<E> model){
	super(model);
	this.defaultSortingListener = new SorterEventListener<E>() {

	    @Override
	    public void valueChanged(final SorterChangedEvent<E> e) {
		repaint();
	    }
	};
	sortingModel = new DefaultSortingModel<E>();
	sortingModel.addSorterEventListener(defaultSortingListener);
	setCellRenderer(new SortableListCellRenderer<E>(this));
    }

    @Override
    public DefaultListModel<E> getModel() {
        return (DefaultListModel<E>)super.getModel();
    }

    public ListSortingModel<E> getSortingModel() {
	return sortingModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setModel(final ListModel<E> newModel) {
	super.setModel(newModel);
	if (getCellRenderer() instanceof SortingCheckboxListCellRenderer) {
	    ((SortableListCellRenderer<E>) getCellRenderer()).updateCellWidth(this);
	}
	final Vector<E> listData = getVectorListData();
	for (final Pair<E, Ordering> sortObject : sortingModel.getSortObjects()) {
	    if (!listData.contains(sortObject.getKey())) {
		resetSortOrder(sortObject.getKey(), sortObject.getValue());
	    }
	}
	repaint();
    }

    /**
     * Set the sorting model for this {@link SortingCheckboxList}.
     *
     * @param sortingModel
     */
    public void setSortingModel(final ListSortingModel<E> newSortingModel) {
	if(sortingModel == newSortingModel){
	    return;
	}
	if (newSortingModel != null) {
	    final DefaultListModel<E> listModel = getModel();
	    for (final Pair<E, Ordering> orderItem : newSortingModel.getSortObjects()) {
		if (!listModel.contains(orderItem.getKey())) {
		    resetSortOrder(orderItem.getKey(), orderItem.getValue());
		}
	    }
	    newSortingModel.addSorterEventListener(defaultSortingListener);
	}
	if(sortingModel != null){
	    sortingModel.removeSorterEventListener(defaultSortingListener);
	}
	this.sortingModel = newSortingModel;
	repaint();
    }

    /**
     * Returns list data.
     *
     * @return
     */
    public Vector<E> getVectorListData() {
	final Vector<E> listData = new Vector<E>();
	for (int index = 0; index < getModel().getSize(); index++) {
	    listData.add(getModel().getElementAt(index));
	}
	return listData;
    }

    public boolean isSelectBySortingEvent() {
	return selectBySortingEvent;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processMouseEvent(final MouseEvent e) {
	if (e.getID() == MouseEvent.MOUSE_PRESSED) {
	    final int x = e.getX();
	    final int y = e.getY();
	    final int row = locationToIndex(e.getPoint());
	    final Rectangle rect = row < 0 ? null : getCellBounds(row, row);
	    final int actualX = rect != null ? x - rect.x : 0;
	    final int actualY = rect != null ? y - rect.y : 0;
	    if (getCellRenderer() instanceof ISortableListCellRenderer) {
		if (row >= 0 && ((ISortableListCellRenderer<E>) getCellRenderer()).isOnOrderingArrow(actualX, actualY)) {
		    if (!e.isControlDown()) {
			sortingModel.toggleSorterSingle(getModel().getElementAt(row));
		    } else {
			sortingModel.toggleSorter(getModel().getElementAt(row));
		    }
		    if (!isSelectBySortingEvent()) {
			return;
		    }
		}
	    }
	}
	super.processMouseEvent(e);
    }

    /**
     * Rests the sort order for the specified pair of sorting value and it's ordering.
     *
     * @param sortObject
     */
    private void resetSortOrder(final E item, final Ordering ordering) {
	getSortingModel().toggleSorter(item);
	if(ordering == Ordering.ASCENDING){
	    getSortingModel().toggleSorter(item);
	}
    }
}
