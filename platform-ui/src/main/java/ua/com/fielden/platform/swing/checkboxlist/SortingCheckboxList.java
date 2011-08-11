package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.ListModel;
import javax.swing.SortOrder;

public class SortingCheckboxList<T> extends CheckboxList<T> {

    private static final long serialVersionUID = -1396817497547523857L;

    private final ListSortingModel<T> sortingModel;

    public SortingCheckboxList(final DefaultListModel model) {
	super(model);
	sortingModel = new DefaultSortingModel<T>();
	sortingModel.setSingle(true);
	addListCheckingListener(new ListCheckingListener<T>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<T> e) {
		if (!e.isChecked() && SortOrder.UNSORTED != sortingModel.getSortOrder(e.getValue())) {
		    final List<SortObject<T>> sortObjects = new ArrayList<SortObject<T>>();
		    sortObjects.add(new SortObject<T>(e.getValue(), SortOrder.UNSORTED));
		    sortingModel.setSortObjects(sortObjects, false);
		}
	    }
	});
	sortingModel.addSorterEventListener(new SorterEventListener<T>() {

	    @Override
	    public void valueChanged(final SorterChangedEvent<T> e) {
		repaint();
	    }

	    @Override
	    public void sortingRangeChanged(final SortRangeChangedEvent e) {
		repaint();
	    }
	});
	setCellRenderer(new SortingCheckboxListCellRenderer<T>(this, new JCheckBox()));
    }

    public ListSortingModel<T> getSortingModel() {
	return sortingModel;
    }

    @Override
    public void setModel(final ListModel newModel) {
	super.setModel(newModel);
	if (getCellRenderer() instanceof SortingCheckboxListCellRenderer) {
	    ((SortingCheckboxListCellRenderer) getCellRenderer()).updateCellWidth(this);
	}
	final Vector<T> listData = getVectorListData();
	final List<SortObject<T>> sortObjects = new ArrayList<SortObject<T>>();
	for (final SortObject<T> sortObject : sortingModel.getSortObjects()) {
	    if (listData.contains(sortObject.getSortObject())) {
		sortObjects.add(sortObject);
	    }
	}
	sortingModel.setSortObjects(sortObjects, true);
    }

    @Override
    protected void processMouseEvent(final MouseEvent e) {
	if (e.getID() == MouseEvent.MOUSE_PRESSED) {
	    final int x = e.getX();
	    final int y = e.getY();
	    final int row = locationToIndex(e.getPoint());
	    final Rectangle rect = row < 0 ? null : getCellBounds(row, row);
	    final int actualX = rect != null ? x - rect.x : 0;
	    final int actualY = rect != null ? y - rect.y : 0;
	    if (getCellRenderer() instanceof SortingCheckingListCellRenderer) {
		if (row >= 0 && isValueChecked((T) getModel().getElementAt(row)) && //
			((SortingCheckingListCellRenderer) getCellRenderer()).isOnOrderingArrow(actualX, actualY)) {
		    sortingModel.toggleSorter((T) getModel().getElementAt(row), e.isControlDown() ? false : true);
		    if (!isSelectsByChecking()) {
			return;
		    }
		}
	    }
	}
	super.processMouseEvent(e);
    }
}
