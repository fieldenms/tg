package ua.com.fielden.platform.swing.checkboxlist;

/**
 * The model for checking/unchecking the values of a {@link CheckboxList}. Also allows to check multiple or single values.
 * 
 * @author oleh
 */
public interface ListCheckingModel<T> {

    /**
     * Checks or uncheckes specified value.
     * 
     * @param value
     *            - the value to be added.
     */
    public void checkValue(T value, boolean check);

    /**
     * Returns the values those are checked.
     */
    public T[] getCheckingValues(T[] values);

    /**
     * Returns the values those are checked.
     * 
     * @return
     */
    public Object[] getCheckingValues();

    /**
     * Returns true if the item specifed with the value is currently checked.
     * 
     * @param value
     *            - an <code>item</code> identifying a list element.
     * @return true if the list element is checked
     */
    public boolean isValueChecked(T value);

    /**
     * Alter (check/uncheck) the checking state of the specified list element if possible.
     */
    public void toggleCheckingValue(T value);

    /**
     * Adds the specified listener to the list of listeners that are notified each time the set of checking values changes.
     * 
     * @param listener
     *            the new listener to be added
     */
    public void addListCheckingListener(ListCheckingListener<T> listener);

    /**
     * Removes listener from the list of listeners that are notified each time the set of checking values changes.
     * 
     * @param listener
     *            - the listener to remove
     */
    public void removeListCheckingListener(ListCheckingListener<T> listener);

}
