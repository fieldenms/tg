package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListModel;

public class CheckboxList<T> extends JList<T> {

    private static final long serialVersionUID = -8861596050387769319L;

    private ListCheckingModel<T> checkingModel;

    private ListCheckingListener<T> defaultCheckingListener;

    /**
     * Whether checking a node causes it to be selected, too.
     */
    private boolean selectsByChecking = true;

    /**
     * Creates {@link CheckboxList} with default {@link ListModel}.
     *
     * @param model
     *            - specified {@link DefaultListModel}.
     */
    public CheckboxList(final DefaultListModel<T> model) {
	super(model);
	this.defaultCheckingListener = new ListCheckingListener<T>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<T> e) {
		repaint();
	    }
	};
	this.checkingModel = new DefaultListCheckingModel<T>();
	checkingModel.addListCheckingListener(defaultCheckingListener);
	setCellRenderer(new CheckboxListCellRenderer<T>(new JCheckBox()));
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
    public void checkValue(final T value, final boolean check) {
	checkingModel.checkValue(value, check);
    }

    /**
     * Adds a listener for {@link ListCheckingEvent} to the {@link ListCheckingModel}.
     *
     * @param listener
     *            - the {@link ListCheckingListener} that will be notified when a value is checked.
     */
    public void addListCheckingListener(final ListCheckingListener<T> listener) {
	checkingModel.addListCheckingListener(listener);
    }

    /**
     * Returns list elements those are in the checked values set.
     *
     * @param indexs
     */
    public T[] getCheckingValues(final T[] values) {
	return checkingModel.getCheckingValues(values);
    }

    /**
     * Returns true if the item identified by the value is currently checked for the {@link ListCheckingModel}.
     *
     * @param value
     *            - an {@link T} identifying a list element.
     * @return true if the value is checked
     */
    public boolean isValueChecked(final T value) {
	return checkingModel.isValueChecked(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processMouseEvent(final MouseEvent e) {
	if (e.getID() == MouseEvent.MOUSE_PRESSED) {
	    if (!e.isConsumed()) {
		// we use mousePressed instead of mouseClicked for performance
		final int x = e.getX();
		final int y = e.getY();
		final int row = locationToIndex(e.getPoint());

		if (row >= 0) {
		    // click inside some value
		    final Rectangle rect = getCellBounds(row, row);
		    if (rect != null && getCellRenderer() instanceof CheckingListCellRenderer) {
			// click on a value's hot spot
			if (((CheckingListCellRenderer<T>) getCellRenderer()).isOnHotSpot(x - rect.x, y - rect.y)) {
			    checkingModel.toggleCheckingValue(getModel().getElementAt(row));
			    if (!isSelectsByChecking()) {
				return;
			    }
			}
		    }
		}
	    }
	}
	super.processMouseEvent(e);
    }

    /**
     * Removes a {@link ListCheckingListener} from the {@link ListCheckingModel} property.
     *
     * @param listener
     *            - the {@link ListCheckingListener} to remove.
     */
    public void removeListCheckingListener(final ListCheckingListener<T> listener) {
	checkingModel.removeListCheckingListener(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setModel(final ListModel<T> newModel) {
	if(!(newModel instanceof DefaultListModel)){
	    throw new IllegalArgumentException("It's impossible to set different then default list model");
	}
	super.setModel(newModel);
	final Vector<T> newValues = getVectorListData();
	for (final Object value : getCheckingValues()) {
	    if (!newValues.contains(value)) {
		checkValue((T) value, false);
	    }
	}
	repaint();
    }

    @Override
    public DefaultListModel<T> getModel() {
        return (DefaultListModel<T>)super.getModel();
    }

    /**
     * Set the specific checking model.
     *
     * @param newCheckingModel
     */
    @SuppressWarnings("unchecked")
    public void setCheckingModel(final ListCheckingModel<T> newCheckingModel){
	if(checkingModel == newCheckingModel){
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
	if(checkingModel != null){
	    checkingModel.removeListCheckingListener(defaultCheckingListener);
	}
	this.checkingModel = newCheckingModel;
	repaint();
    }

    private Object[] getCheckingValues() {
	return checkingModel.getCheckingValues();
    }

    /**
     * Returns {@link ListCheckingModel} instance for this list.
     *
     * @return
     */
    public ListCheckingModel<T> getCheckingModel() {
	return checkingModel;
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
    public Vector<T> getSelectedValuesInOrder() {
	final Vector<T> selectedValues = new Vector<T>();
	for (int index = 0; index < getModel().getSize(); index++) {
	    final T value = getModel().getElementAt(index);
	    if (isValueChecked(value)) {
		selectedValues.add(value);
	    }
	}
	return selectedValues;
    }
}
