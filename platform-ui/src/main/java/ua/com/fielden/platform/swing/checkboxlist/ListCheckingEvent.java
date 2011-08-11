package ua.com.fielden.platform.swing.checkboxlist;

import java.util.EventObject;

/**
 * An event that characterizes a change in the current checking. The change is related to a single checked/unchecked value of the list. ListCheckingListeners will generally query
 * the source of the event for the new checked status of each potentially changed row.
 * 
 * @see ListCheckingListener
 * @see ListCheckingModel
 * @author oleh
 */
public class ListCheckingEvent<T> extends EventObject {

    private static final long serialVersionUID = 8037796144208793257L;

    /**
     * The list value related to this event.
     */
    private final T value;
    private final boolean checked;

    /**
     * Represents a change in the checking of a ListCheckingModel. The specified value identifies the value that have been either checked or unchecked.
     * 
     * @param source
     *            - source of event.
     * @param value
     *            - the value that has changed in the checking.
     * @param checked
     *            - whether or not the value is checked, false means that value was removed from the checking.
     */
    public ListCheckingEvent(final Object source, final T value, final boolean checked) {
	super(source);
	this.value = value;
	this.checked = checked;
    }

    /**
     * Returns the value that was added or removed from the checking.
     */
    public T getValue() {
	return value;
    }

    /**
     * Returns true if the value related to the event is checked otherwise returns false.
     */
    public boolean isChecked() {
	return checked;
    }

}
