package ua.com.fielden.platform.swing.review.report.interfaces;

import ua.com.fielden.platform.swing.review.report.events.LoadEvent;

/**
 * Contract listener that allows one to manage load listeners.
 * 
 * @author TG Team
 * 
 */
public interface ILoadNotifier {

    /**
     * Registers the listener that listens the load notifications.
     * 
     * @param listener
     */
    void addLoadListener(ILoadListener listener);

    /**
     * Unregisters the load listener.
     * 
     * @param listener
     */
    void removeLoadListener(ILoadListener listener);

    /**
     * Fires the event that indicates that this {@link ILoadNotifier} was loaded.
     * 
     * @param loadEvent
     */
    void fireLoadEvent(final LoadEvent loadEvent);
}
