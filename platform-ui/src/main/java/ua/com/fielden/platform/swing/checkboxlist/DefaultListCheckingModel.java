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
    private final HashSet<T> disabledValuesSet = new HashSet<T>();

    private CheckingMode checkingMode;

    /**
     * Default constructor set the {@link CheckingMode} property to {@link CheckingMode#MULTIPLE}.
     * 
     * @param model
     */
    public DefaultListCheckingModel() {
	this.checkingMode = CheckingMode.MULTIPLE;
    }

    @Override
    public void addCheckingValue(final T value) {
	if (getCheckingMode() == CheckingMode.SINGLE) {
	    clearChecking();
	}
	if (this.checkedValuesSet.add(value)) {
	    final ListCheckingEvent<T> event = new ListCheckingEvent<T>(this, value, true);
	    fireValueChanged(event);
	}
    }

    @Override
    public void addCheckingValues(final T[] values) {
	for (final T value : values) {
	    addCheckingValue(value);
	}
    }

    @Override
    public void addListCheckingListener(final ListCheckingListener<T> listener) {
	listenerList.add(ListCheckingListener.class, listener);
    }

    @Override
    public void clearChecking() {
	final Object[] valuesToRemove = getCheckingValues();
	checkedValuesSet.clear();
	for (final Object value : valuesToRemove) {
	    fireValueChanged(new ListCheckingEvent<T>(this, (T) value, false));
	}
    }

    @Override
    public CheckingMode getCheckingMode() {
	return checkingMode;
    }

    @Override
    public T[] getCheckingValues(final T[] values) {
	return checkedValuesSet.toArray(values);
    }

    @Override
    public boolean isValueChecked(final T value) {
	return checkedValuesSet.contains(value);
    }

    @Override
    public boolean isValueEnabled(final T value) {
	return !disabledValuesSet.contains(value);
    }

    @Override
    public void removeCheckingValue(final T value) {
	if (checkedValuesSet.remove(value)) {
	    fireValueChanged(new ListCheckingEvent<T>(this, value, false));
	}
    }

    @Override
    public void removeCheckingValues(final T[] values) {
	for (final T value : values) {
	    removeCheckingValue(value);
	}
    }

    @Override
    public void removeListCheckingListener(final ListCheckingListener<T> listener) {
	listenerList.remove(ListCheckingListener.class, listener);
    }

    @Override
    public final void setCheckingMode(final CheckingMode mode) {
	if (mode != null) {
	    this.checkingMode = mode;
	}
    }

    @Override
    public void setCheckingValue(final T value) {
	clearChecking();
	addCheckingValue(value);
    }

    @Override
    public void setCheckingValues(final T[] values) {
	clearChecking();
	addCheckingValues(values);
    }

    @Override
    public void setValueEnabled(final T value, final boolean enable) {
	if (enable) {
	    disabledValuesSet.remove(value);
	} else {
	    disabledValuesSet.add(value);
	}
    }

    @Override
    public void setValuesEnabled(final T[] values, final boolean enable) {
	for (final T value : values) {
	    setValueEnabled(value, enable);
	}
    }

    @Override
    public void toggleCheckingValue(final T value) {
	if (!isValueEnabled(value)) {
	    return;
	}
	if (isValueChecked(value)) {
	    removeCheckingValue(value);
	} else {
	    addCheckingValue(value);
	}
    }

    /**
     * Notifies all listeners that are registered for list checking events on this T.
     * 
     * @see #addListCheckingListener
     * @see EventListenerList
     */
    protected void fireValueChanged(final ListCheckingEvent<T> event) {
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ListCheckingListener.class) {
		((ListCheckingListener) listeners[i + 1]).valueChanged(event);
	    }
	}
    }

    @Override
    public Object[] getCheckingValues() {
	return checkedValuesSet.toArray();
    }

}
