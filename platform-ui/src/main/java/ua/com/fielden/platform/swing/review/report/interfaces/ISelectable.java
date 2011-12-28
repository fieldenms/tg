package ua.com.fielden.platform.swing.review.report.interfaces;

/**
 * Contract for anything that is interested in receiving selection events.
 * 
 * @author TG Team
 *
 */
public interface ISelectable {

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
