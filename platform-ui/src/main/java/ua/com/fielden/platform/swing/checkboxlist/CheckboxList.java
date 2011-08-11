package ua.com.fielden.platform.swing.checkboxlist;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListModel;

public class CheckboxList<T> extends JList {

    private static final long serialVersionUID = -8861596050387769319L;

    private final ListCheckingModel<T> checkingModel;

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
    public CheckboxList(final DefaultListModel model) {
	super(model);
	this.checkingModel = new DefaultListCheckingModel<T>();
	checkingModel.addListCheckingListener(new ListCheckingListener<T>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<T> e) {
		repaint();
	    }
	});
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
    public void addCheckingValue(final T value) {
	checkingModel.addCheckingValue(value);
    }

    /**
     * Add values to the checked values set.
     * 
     * @param values
     *            - specified values to be added.
     */
    public void addCheckingValues(final T[] values) {
	checkingModel.addCheckingValues(values);
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
     * Clears the checking for the {@link ListCheckingModel}.
     * 
     */
    public void clearChecking() {
	checkingModel.clearChecking();
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

    /**
     * Returns true if the item specified with value is not in disabled values set.
     * 
     * @param value
     *            - value to check whether it is enabled or disabled.
     * @return
     */
    public boolean isValueEnabled(final T value) {
	return checkingModel.isValueEnabled(value);
    }

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
			if (((CheckingListCellRenderer) getCellRenderer()).isOnHotSpot(x - rect.x, y - rect.y)) {
			    checkingModel.toggleCheckingValue((T) getModel().getElementAt(row));
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
     * Remove a value from the checked values set.
     * 
     * @param value
     *            - value to be removed.
     */
    public void removeCheckingValue(final T value) {
	checkingModel.removeCheckingValue(value);
    }

    /**
     * Remove list elements from the checked values set of the {@link ListCheckingModel}.
     * 
     * @param values
     */
    public void removeCheckingValues(final T[] values) {
	checkingModel.removeCheckingValues(values);
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

    /**
     * Set checking value to the {@link TreeCheckingModel}'s checking values set.
     * 
     * @param value
     */
    public void setCheckingValue(final T value) {
	checkingModel.setCheckingValue(value);
    }

    /**
     * Set values to the {@link ListCheckingModel}'s checking values set.
     * 
     * @param values
     */
    public void setCheckingValues(final T[] values) {
	checkingModel.setCheckingValues(values);
    }

    @Override
    public void setModel(final ListModel newModel) {
	super.setModel(newModel);
	final Vector<T> newValues = getVectorListData();
	for (final Object value : getCheckingValues()) {
	    if (!newValues.contains(value)) {
		removeCheckingValue((T) value);
	    }
	}
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
	    listData.add((T) getModel().getElementAt(index));
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
	    final T value = (T) getModel().getElementAt(index);
	    if (isValueChecked(value)) {
		selectedValues.add(value);
	    }
	}
	return selectedValues;
    }
}
