package ua.com.fielden.platform.swing.checkboxlist;

import java.util.HashSet;

import javax.swing.event.EventListenerList;

/**
 * The default list checking model, providing methods for storing checked list elements and retrieving them.
 *
 * @author oleh
 *
 */
public class DefaultListCheckingModel<T> implements ListCheckingModel<T> {

    /** Event listener list. */
    private final EventListenerList listenerList = new EventListenerList();

    private final HashSet<T> checkedValuesSet = new HashSet<T>();

    @Override
    public void checkValue(final T value, final boolean check) {
	if(check && !checkedValuesSet.contains(value)){
	    checkedValuesSet.add(value);
	    fireValueChanged(new ListCheckingEvent<T>(this, value, false, true));
	} else if(!check && checkedValuesSet.contains(value)){
	    checkedValuesSet.remove(value);
	    fireValueChanged(new ListCheckingEvent<T>(this, value, true, false));
	}
    }

    @Override
    public T[] getCheckingValues(final T[] values) {
	return checkedValuesSet.toArray(values);
    }

    @Override
    public Object[] getCheckingValues() {
	return checkedValuesSet.toArray();
    }

    @Override
    public boolean isValueChecked(final T value) {
	return checkedValuesSet.contains(value);
    }

    @Override
    public void toggleCheckingValue(final T value) {
	if (isValueChecked(value)) {
	    checkValue(value, false);
	} else {
	    checkValue(value, true);
	}
    }

    @Override
    public void addListCheckingListener(final ListCheckingListener<T> listener) {
	listenerList.add(ListCheckingListener.class, listener);
    }

    @Override
    public void removeListCheckingListener(final ListCheckingListener<T> listener) {
	listenerList.remove(ListCheckingListener.class, listener);
    }

    /**
     * Notifies all listeners that are registered for list checking events on this T.
     *
     * @see #addListCheckingListener
     * @see EventListenerList
     */
    @SuppressWarnings("unchecked")
    protected void fireValueChanged(final ListCheckingEvent<T> event) {
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ListCheckingListener.class) {
		((ListCheckingListener<T>) listeners[i + 1]).valueChanged(event);
	    }
	}
    }
}
