package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;

/**
 * The event listener that is notified when the Close or Select actions on the locator were performed.
 * 
 * @author TG Team
 * 
 */
public interface ILocatorEventListener extends EventListener {

    /**
     * Performs custom task after the locator action was performed.
     * 
     * @param event
     */
    void locatorActionPerformed(LocatorEvent event);
}
