package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.LoadEvent;

/**
 * The {@link EventListener} that listens the load notifications.
 * 
 * @author TG Team
 * 
 */
public interface ILoadListener extends EventListener {

    /**
     * Is notified when the specified view was loaded.
     * 
     * @param event
     */
    void viewWasLoaded(LoadEvent event);
}
