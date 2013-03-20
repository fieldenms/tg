package ua.com.fielden.platform.pagination;

import java.util.EventListener;

import ua.com.fielden.platform.pagination.IPage;


/**
 * An {@link EventListener} that listens the page changed events.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPageChangedListener extends EventListener {

    /**
     * Notifies that the specific {@link IPage} has changed.
     *
     * @param e
     */
    void pageChanged(PageChangedEvent e);
}
