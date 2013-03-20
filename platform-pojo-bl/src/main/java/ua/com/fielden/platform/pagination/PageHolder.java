package ua.com.fielden.platform.pagination;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPaginatorModel.PageNavigationPhases;

/**
 * Holds the page and allows to change page and provides ability to listen page changing events.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class PageHolder {

    private final EventListenerList listenerList = new EventListenerList();

    private IPage<? extends AbstractEntity<?>> page;

    /**
     * Sets the new page for this page holder. This method also fires page changed event. Please note that the event processing will be performed on EDT.
     *
     * @param newPage
     */
    public void newPage(final IPage<? extends AbstractEntity<?>> newPage) {
	this.page = newPage;
	firePageChanged(new PageChangedEvent(PageHolder.this, newPage));
    }

    /**
     * Returns the current page.
     *
     * @return
     */
    public IPage<? extends AbstractEntity<?>> getPage() {
	return page;
    }

    /**
     * Adds the specified {@link IPageChangedListener} that listens the page changed events.
     *
     * @param l
     */
    public void addPageChangedListener(final IPageChangedListener l) {
	listenerList.add(IPageChangedListener.class, l);
    }

    /**
     * Removes the specified {@link IPageChangedListener} that listens the page changed events.
     *
     * @param l
     */
    public void removePageChangedListener(final IPageChangedListener l) {
	listenerList.remove(IPageChangedListener.class, l);
    }

    /**
     * Adds the specified {@link IPageNavigationListener} that listens the page navigation events.
     *
     * @param l
     */
    public void addPageNavigationListener(final IPageNavigationListener l) {
	listenerList.add(IPageNavigationListener.class, l);
    }

    /**
     * Removes the specified {@link IPageNavigationListener} that listens the page navigation events.
     *
     * @param l
     */
    public void removePageNavigationListener(final IPageNavigationListener l) {
	listenerList.remove(IPageNavigationListener.class, l);
    }

    /**
     * Fires the {@link PageChangedEvent} that indicates the page changing event.
     *
     * @param pageChangedEvent
     */
    private void firePageChanged(final PageChangedEvent pageChangedEvent) {
	for (final IPageChangedListener l : listenerList.getListeners(IPageChangedListener.class)) {
	    l.pageChanged(pageChangedEvent);
	}
    }

    /**
     * Processes some actions during page navigation phases.
     *
     * @param pageNavigationPhases
     */
    public void pageNavigated(final PageNavigationPhases pageNavigationPhases) {
	final PageNavigationEvent event = new PageNavigationEvent(this, pageNavigationPhases);
	for (final IPageNavigationListener l : listenerList.getListeners(IPageNavigationListener.class)) {
	    l.pageNavigated(event);
	}
    }
}
