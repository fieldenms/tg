package ua.com.fielden.platform.expression.editor;

/**
 * A contract for anything that can provide property. (e. g. entities tree can select property that can be used in the expression editor).
 * 
 * @author TG Team
 * 
 */
public interface IPropertyProvider {

    /**
     * Notifies that property was selected.
     * 
     * @param propertyName
     *            - selected property.
     * @param isSelect
     *            - indicates the state of the property.
     */
    void propertyStateChanged(String propertyName, boolean isSelect);

    /**
     * Adds specified {@link IPropertySelectionListener} that listens property selection event.
     * 
     * @param l
     *            - specified {@link IPropertySelectionListener} to be added.
     */
    void addPropertySelectionListener(IPropertySelectionListener l);

    /**
     * Removes specified {@link IPropertySelectionListener} instance.
     * 
     * @param l
     *            - specified IPropertySelectionListener to be removed.
     */
    void removePropertySelectionListener(IPropertySelectionListener l);

}
