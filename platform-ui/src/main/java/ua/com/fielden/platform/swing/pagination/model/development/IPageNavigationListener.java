package ua.com.fielden.platform.swing.pagination.model.development;

import java.util.EventListener;

/**
 * An {@link EventListener} that listens the {@link PageNavigationEvent}s.
 * 
 * @author TG Team
 *
 */
public interface IPageNavigationListener extends EventListener {

    /**
     * Invoked when the specific page of the {@link PageHolder} was navigated.
     * 
     * @param event
     */
    void pageNavigated(PageNavigationEvent event);
}
