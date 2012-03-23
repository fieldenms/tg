package ua.com.fielden.platform.swing.pagination.model.development;

import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.pagination.Paginator;

/**
 * The model for the {@link Paginator}. This model supports multiple pagination.
 *
 * @author TG Team
 *
 */
public class PaginatorModel implements IPaginatorModel, IPageHolderManager{

    private final Set<PageHolder> pageHolderSet = new HashSet<PageHolder>();
    private PageHolder currentPageHolder;

    private final EventListenerList listenerList = new EventListenerList();

    /**
     * This is common {@link IPageChangedListener} for all page holders.
     * This page changed listener calls paginator's model {@link #firePageChangedEvent(PageChangedEvent)}
     * only if the event's holder is equal to the current page holder.
     */
    private final IPageChangedListener commonPageChangedListener = new IPageChangedListener() {

	@Override
	public void pageChanged(final PageChangedEvent e) {
	    if(e.getSource() == currentPageHolder){
		firePageChangedEvent(e);
	    }
	}

    };

    @Override
    public void selectPageHolder(final PageHolder pageHolder) {
	if(pageHolderSet.contains(pageHolder)){
	    currentPageHolder = pageHolder;
	    firePageHolderChanged(new PageHolderChangedEvent(this, pageHolder));
	} else {
	    throw new IllegalArgumentException("The specified page holder must be added first in order to be selected.");
	}
    }

    @Override
    public IPage2<? extends AbstractEntity> getCurrentPage() {
	return getCurrentPageHolder() == null ? null : getCurrentPageHolder().getPage();
    }

    @Override
    public void addPageHolder(final PageHolder pageHolder) {
	if(pageHolder != null && pageHolderSet.add(pageHolder)){
	    pageHolder.addPageChangedListener(commonPageChangedListener);
	}
    }

    @Override
    public void removePageHolder(final PageHolder pageHolder) {
	if(pageHolder != null && pageHolderSet.remove(pageHolder)){
	    pageHolder.removePageChangedListener(commonPageChangedListener);
	}
    }

    @Override
    public PageHolder getCurrentPageHolder() {
	return currentPageHolder;
    }

    @Override
    public void firstPage() {
	if (getCurrentPage() != null) {
	    getCurrentPageHolder().newPage(getCurrentPage().first());
	}
    }

    @Override
    public void prevPage() {
	if (getCurrentPage() != null) {
	    getCurrentPageHolder().newPage(getCurrentPage().prev());
	}
    }

    @Override
    public void nextPage() {
	if (getCurrentPage() != null) {
	    getCurrentPageHolder().newPage(getCurrentPage().next());
	}
    }

    @Override
    public void lastPage() {
	if (getCurrentPage() != null) {
	    getCurrentPageHolder().newPage(getCurrentPage().last());
	}
    }

    @Override
    public void addPageChangedListener(final IPageChangedListener l) {
	listenerList.add(IPageChangedListener.class, l);
    }

    @Override
    public void removePageChangedListener(final IPageChangedListener l) {
	listenerList.remove(IPageChangedListener.class, l);
    }

    @Override
    public void addPageHolderChangedListener(final IPageHolderChangedListener l) {
	listenerList.add(IPageHolderChangedListener.class, l);
    }

    @Override
    public void removePageHolderChangedListener(final IPageHolderChangedListener l) {
	listenerList.remove(IPageHolderChangedListener.class, l);
    }

    private void firePageHolderChanged(final PageHolderChangedEvent pageHolderChangedEvent) {
	for(final IPageHolderChangedListener l : listenerList.getListeners(IPageHolderChangedListener.class)){
	    l.pageHolderChanged(pageHolderChangedEvent);
	}
    }

    private void firePageChangedEvent(final PageChangedEvent pageChangedEvent){
	for(final IPageChangedListener l : listenerList.getListeners(IPageChangedListener.class)){
	    l.pageChanged(pageChangedEvent);
	}
    }

    @Override
    public void pageNavigationPhases(final PageNavigationPhases pageNavigationPhases) {
	if(getCurrentPageHolder() != null){
	    getCurrentPageHolder().pageNavigated(pageNavigationPhases);
	}
    }
}
