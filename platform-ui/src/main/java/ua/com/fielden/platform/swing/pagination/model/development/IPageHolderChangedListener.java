package ua.com.fielden.platform.swing.pagination.model.development;

import java.util.EventListener;

/**
 * An {@link EventListener} that listens the page holder changed events.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IPageHolderChangedListener extends EventListener {

    /**
     * Notifies that specific {@link PageHolder} changed.
     * 
     * @param e
     */
    void pageHolderChanged(PageHolderChangedEvent e);
}
