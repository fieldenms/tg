package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

public class SortingCheckboxList<T> extends JList<T> {

    private static final long serialVersionUID = -1396817497547523857L;

    /**
     * Related to sorting model of the list
     */
    private ListSortingModel<T> sortingModel;
    private final SorterEventListener<T> defaultSortingListener;

    /**
     * Related to checking model of the list.
     */
    private final List<ListCheckingModel<T>> checkingModels;
    private final ListCheckingListener<T> defaultCheckingListener;

    /**
     * Whether checking a node causes it to be selected, too.
     */
    private boolean selectsByChecking = true;

    public SortingCheckboxList(final DefaultListModel<T> model, final int checkingModelCount) {
	super(model);

	this.checkingModels = new ArrayList<>(checkingModelCount);
	this.defaultCheckingListener = new ListCheckingListener<T>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<T> e) {
		repaint();
	    }
	};
	for(int modelCount = 0; modelCount < checkingModelCount; modelCount++){
	    final DefaultListCheckingModel<T> checkingModel = new DefaultListCheckingModel<>();
	    checkingModel.addListCheckingListener(defaultCheckingListener);
	    checkingModels.add(checkingModel);
	}
	this.sortingModel = new DefaultSortingModel<T>();
	this.defaultSortingListener = new SorterEventListener<T>() {

	    @Override
	    public void valueChanged(final SorterChangedEvent<T> e) {
		repaint();
	    }
	};
	this.sortingModel.addSorterEventListener(defaultSortingListener);
	setCellRenderer(new SortingCheckboxListCellRenderer<T>(this){

	    private static final long serialVersionUID = 4095565358540330029L;

	    @Override
	    public boolean isSortingAvailable(final T element) {
		boolean atLeastOneSelected = false;
		for(final ListCheckingModel<T> checkingModel : checkingModels){
		    atLeastOneSelected |= sortingModel.isSortable(element) && checkingModel.isValueChecked(element);
		}
		return atLeastOneSelected;
	    }

	});
    }

    /**
     * Specifies whether checking a list element causes it to be selected, too, or else the selection is not affected. The default behaviour is the former.
     *
     * @param selectsByChecking
     */
    public void setSelectsByChecking(final boolean selectsByChecking) {
	this.selectsByChecking = selectsByChecking;
    }

    /**
     * Returns whether checking a list element causes it to be selected, too.
     *
     * @return
     */
    public boolean isSelectsByChecking() {
	return selectsByChecking;
    }

    /**
     * Add a value to the checked values set.
     *
     * @param value
     *            - specified value to be added.
     */
    public void checkValue(final T value, final int index, final boolean check) {
	checkingModels.get(index).checkValue(value, check);
    }

    /**
     * Adds a listener for {@link ListCheckingEvent} to the {@link ListCheckingModel}.
     *
     * @param listener
     *            - the {@link ListCheckingListener} that will be notified when a value is checked.
     */
    public void addListCheckingListener(final ListCheckingListener<T> listener, final int index) {
	checkingModels.get(index).addListCheckingListener(listener);
    }

    /**
     * Removes a {@link ListCheckingListener} from the {@link ListCheckingModel} property.
     *
     * @param listener
     *            - the {@link ListCheckingListener} to remove.
     */
    public void removeListCheckingListener(final ListCheckingListener<T> listener, final int index) {
	checkingModels.get(index).removeListCheckingListener(listener);
    }

    /**
     * Returns list elements those are in the checked values set.
     *
     * @param indexs
     */
    public T[] getCheckingValues(final T[] values, final int index) {
	return checkingModels.get(index).getCheckingValues(values);
    }

    /**
     * Returns true if the item identified by the value is currently checked for the {@link ListCheckingModel}.
     *
     * @param value
     *            - an {@link T} identifying a list element.
     * @return true if the value is checked
     */
    public boolean isValueChecked(final T value, final int index) {
	return checkingModels.get(index).isValueChecked(value);
    }

    /**
     * Returns the checking model count.
     *
     * @return
     */
    public int getCheckingModelCount(){
	return checkingModels.size();
    }

    /**
     * Returns the sorting model of the list.
     *
     * @return
     */
    public ListSortingModel<T> getSortingModel() {
	return sortingModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setModel(final ListModel<T> newModel) {
	if(!(newModel instanceof DefaultListModel)){
	    throw new IllegalArgumentException("It's impossible to set different then default list model");
	}
	super.setModel(newModel);
	if (getCellRenderer() instanceof SortingCheckboxListCellRenderer) {
	    ((SortingCheckboxListCellRenderer<T>) getCellRenderer()).updateCellWidth(this);
	}
	//Updating the checking models.
	final Vector<T> newValues = getVectorListData();
	for (int modelIndex = 0; modelIndex < checkingModels.size(); modelIndex++) {
	    for (final Object value : getCheckingValues(modelIndex)) {
		if (!newValues.contains(value)) {
		    checkValue((T) value, modelIndex, false);
		}
	    }
	}
	//Updating the sorting model.
	for (final Pair<T, Ordering> sortObject : sortingModel.getSortObjects()) {
	    if (!newValues.contains(sortObject.getKey())) {
		resetSortOrder(sortObject.getKey(), sortObject.getValue());
	    }
	}
	repaint();
    }

    @Override
    public DefaultListModel<T> getModel() {
        return (DefaultListModel<T>)super.getModel();
    }

    /**
     * Set the specific checking model at the modelIndex.
     *
     * @param newCheckingModel
     */
    @SuppressWarnings("unchecked")
    public void setCheckingModel(final ListCheckingModel<T> newCheckingModel, final int modelIndex){
	if(checkingModels.contains(newCheckingModel)){
	    return;
	}
	if (newCheckingModel != null) {
	    final DefaultListModel<T> listModel = getModel();
	    for (final Object item : newCheckingModel.getCheckingValues()) {
		if (!listModel.contains(item)) {
		    newCheckingModel.checkValue((T) item, false);
		}
	    }
	    newCheckingModel.addListCheckingListener(defaultCheckingListener);
	}
	if(checkingModels.get(modelIndex) != null){
	    checkingModels.get(modelIndex).removeListCheckingListener(defaultCheckingListener);
	}
	checkingModels.set(modelIndex, newCheckingModel);
	repaint();
    }

    /**
     * Returns {@link ListCheckingModel} instance for this list.
     *
     * @return
     */
    public ListCheckingModel<T> getCheckingModel(final int modelIndex) {
	return checkingModels.get(modelIndex);
    }


    /**
     * Returns list data.
     *
     * @return
     */
    public Vector<T> getVectorListData() {
	final Vector<T> listData = new Vector<T>();
	for (int index = 0; index < getModel().getSize(); index++) {
	    listData.add(getModel().getElementAt(index));
	}
	return listData;
    }

    /**
     * Returns checked list data in order that they appear in list.
     *
     * @return
     */
    public Vector<T> getSelectedValuesInOrder(final int modelIndex) {
	final Vector<T> selectedValues = new Vector<T>();
	for (int index = 0; index < getModel().getSize(); index++) {
	    final T value = getModel().getElementAt(index);
	    if (isValueChecked(value, modelIndex)) {
		selectedValues.add(value);
	    }
	}
	return selectedValues;
    }

    /**
     * Set the sorting model for this {@link SortingCheckboxList}.
     *
     * @param sortingModel
     */
    public void setSortingModel(final ListSortingModel<T> newSortingModel) {
	if(sortingModel == newSortingModel){
	    return;
	}
	if (newSortingModel != null) {
	    final DefaultListModel<T> listModel = getModel();
	    for (final Pair<T, Ordering> orderItem : newSortingModel.getSortObjects()) {
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

    public boolean isSortable(final T item) {
        return getSortingModel().isSortable(item);
    }

    @Override
    protected void processMouseEvent(final MouseEvent e) {
	if (e.getID() == MouseEvent.MOUSE_PRESSED && !e.isConsumed()) {
	    final int row = locationToIndex(new Point(e.getX(), e.getY()));
	    final Point p = calculateCellRendererPoint(e.getPoint(), row);
	    final boolean success = performChecking(p, row) || performSorting(p, row, e.isControlDown());
	    if (success && !isSelectsByChecking()) {
		return;
	    }
	}
	super.processMouseEvent(e);
    }

    /**
     * Performs sorting of the element in the row. Sorting will be performed successfully if p is within ordering arrow component.
     *
     * @param p
     * @param row
     * @param isSingle - determines whether it is single sorting or multiple.
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean performSorting(final Point p, final int row, final boolean isSingle) {
	if (p != null && row >= 0
		&& getCellRenderer() instanceof ISortableCheckboxListCellRenderer
		&& ((ISortableCheckboxListCellRenderer<T>) getCellRenderer()).isSortingAvailable(getModel().getElementAt(row))
		&& ((ISortableCheckboxListCellRenderer<T>) getCellRenderer()).isOnOrderingArrow(p.x, p.y)) {
	    if (!isSingle) {
		sortingModel.toggleSorterSingle(getModel().getElementAt(row));
	    } else {
		sortingModel.toggleSorter(getModel().getElementAt(row));
	    }
	    return true;
	}
	return false;
    }

    /**
     * Performs checking of the element in the row. The checking model is determined with parameter p.
     *
     * @param p
     * @param row
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean performChecking(final Point p, final int row) {
	if (p != null && row >= 0 && getCellRenderer() instanceof ISortableCheckboxListCellRenderer) {
	    final int modelIndex = ((ISortableCheckboxListCellRenderer<T>) getCellRenderer()).getHotSpot(p.x, p.y);
	    if (modelIndex >= 0) {
		checkingModels.get(modelIndex).toggleCheckingValue(getModel().getElementAt(row));
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the point that is related to the bonds of the row specified with x and y coordinate.
     *
     * @param point
     * @param y
     * @return
     */
    private Point calculateCellRendererPoint(final Point point, final int row) {
	final Rectangle rect = getCellBounds(row, row);
	return rect == null ? null :new Point(point.x - rect.x, point.y - rect.y);
    }

    /**
     * Rests the sort order for the specified pair of sorting value and it's ordering.
     *
     * @param sortObject
     */
    private void resetSortOrder(final T item, final Ordering ordering) {
        getSortingModel().toggleSorter(item);
        if(ordering == Ordering.ASCENDING){
            getSortingModel().toggleSorter(item);
        }
    }

    private Object[] getCheckingValues(final int index) {
	return checkingModels.get(index).getCheckingValues();
    }
}
