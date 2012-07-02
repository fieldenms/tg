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
    private final T item;
    /**
     * Determines the old and the new state of the item.
     */
    private final Boolean oldCheck;
    private final Boolean newCheck;

   /**
    * Initiates the list checking event with item that changed it's checking and new and old checking values.
    *
    * @param source
    * @param item
    * @param oldCheck
    * @param newCheck
    */
    public ListCheckingEvent(final Object source, final T item, final Boolean oldCheck, final Boolean newCheck) {
	super(source);
	this.item = item;
	this.oldCheck = oldCheck;
	this.newCheck = newCheck;
    }

    /**
     * Returns the value that was added or removed from the checking.
     */
    public T getItem() {
	return item;
    }

    /**
     * Returns the old checking state.
     *
     * @return
     */
    public Boolean getOldCheck() {
	return oldCheck;
    }

    /**
     * Returns the new checking state.
     *
     * @return
     */
    public Boolean getNewCheck() {
	return newCheck;
    }

}
