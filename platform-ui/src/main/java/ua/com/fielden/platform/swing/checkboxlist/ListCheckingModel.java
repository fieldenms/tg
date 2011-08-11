package ua.com.fielden.platform.swing.checkboxlist;

/**
 * The model for checking/unchecking the values of a {@link CheckboxList}. Also allows to check multiple or single values.
 * 
 * @author oleh
 */
public interface ListCheckingModel<T> {

    /**
     * Provides checking behaviours for {@link ListCheckingModel}.
     * 
     * @author oleh
     * 
     */
    public enum CheckingMode {
	SINGLE, MULTIPLE,
    }

    /**
     * Add a value to the checked value set.
     * 
     * @param value
     *            - the value to be added.
     */
    public void addCheckingValue(T value);

    /**
     * Add values to the checked values set.
     * 
     * @param values
     *            - the values to be added.
     */
    public void addCheckingValues(T[] values);

    /**
     * Adds the specified listener to the list of listeners that are notified each time the set of checking values changes.
     * 
     * @param listener
     *            the new listener to be added
     */
    public void addListCheckingListener(ListCheckingListener<T> listener);

    /**
     * Clears the checking.
     */
    public void clearChecking();

    /**
     * Returns the {@link CheckingMode}.
     */
    public CheckingMode getCheckingMode();

    /**
     * Returns the values that are in the checking set.
     */
    public T[] getCheckingValues(T[] values);

    /**
     * Returns the values those are in the checking set.
     * 
     * @return
     */
    public Object[] getCheckingValues();

    /**
     * Returns true if the item identified by the value is currently checked.
     * 
     * @param value
     *            - an <code>item</code> identifying a list element.
     * @return true if the list element is checked
     */
    public boolean isValueChecked(T value);

    /**
     * Returns whether the specified value checking state can be toggled.
     */
    public boolean isValueEnabled(T value);

    /**
     * Remove a value from the checked values set.
     * 
     * @param value
     *            - the list element to be removed.
     */
    public void removeCheckingValue(T value);

    /**
     * Remove values from the checked values set.
     * 
     * @param values
     *            - the values to be removed.
     */
    public void removeCheckingValues(T[] values);

    /**
     * Removes listener from the list of listeners that are notified each time the set of checking values changes.
     * 
     * @param listener
     *            - the listener to remove
     */
    public void removeListCheckingListener(ListCheckingListener<T> listener);

    /**
     * Set the checking mode.
     * 
     * @param mode
     *            - the {@link CheckingMode} to set.
     */
    public void setCheckingMode(CheckingMode mode);

    /**
     * Set the checking to value.
     */
    public void setCheckingValue(T value);

    /**
     * Set the checking to values.
     */
    public void setCheckingValues(T[] values);

    /**
     * Sets whether or not the value is enabled.
     * 
     * @param value
     *            - the list element to enable/disable
     */
    public void setValueEnabled(T value, boolean enable);

    /**
     * Sets whether or not the values are enabled.
     * 
     * @param values
     *            - the list elements to enable/disable
     */
    public void setValuesEnabled(T[] values, boolean enable);

    /**
     * Alter (check/uncheck) the checking state of the specified list element if possible.
     */
    public void toggleCheckingValue(T value);
}
