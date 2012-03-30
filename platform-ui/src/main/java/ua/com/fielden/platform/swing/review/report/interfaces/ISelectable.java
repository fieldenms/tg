package ua.com.fielden.platform.swing.review.report.interfaces;

import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;

/**
 * Contract for anything that is interested in receiving selection events.
 * 
 * @author TG Team
 *
 */
public interface ISelectable {

    /**
     * Performs selection and fires {@link SelectionEvent}.
     */
    public void select();
    /**
     * Adds {@link ISelectionEventListener} instance to listen selection events.
     * 
     * @param l
     */
    void addSelectionEventListener(ISelectionEventListener l);

    /**
     * Removes {@link ISelectionEventListener} instance.
     * 
     * @param l
     */
    void removeSelectionEventListener(ISelectionEventListener l);
}
