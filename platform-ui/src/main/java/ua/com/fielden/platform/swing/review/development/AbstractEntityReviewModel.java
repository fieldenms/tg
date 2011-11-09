package ua.com.fielden.platform.swing.review.development;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.events.DataLoadedEvent;
import ua.com.fielden.platform.swing.review.report.events.PageLoadedEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IDataLoadedEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.IDataLoader;
import ua.com.fielden.platform.swing.review.report.interfaces.IPageLoadedEventListener;

public abstract class AbstractEntityReviewModel<T extends AbstractEntity, DTM extends IDomainTreeManager> implements IDataLoader{

    private final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria;

    private final EventListenerList listenerList;

    public AbstractEntityReviewModel(final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria){
	this.listenerList = new EventListenerList();
	this.criteria = criteria;
    }

    public EntityQueryCriteria<DTM, T, IEntityDao<T>> getCriteria() {
	return criteria;
    }

    @Override
    public void addDataLoadedEventListener(final IDataLoadedEventListener l) {
	listenerList.add(IDataLoadedEventListener.class, l);
    }

    @Override
    public void removeDataLoadedEventListener(final IDataLoadedEventListener l) {
	listenerList.remove(IDataLoadedEventListener.class, l);
    }

    @Override
    public void addPageLoadedEventListener(final IPageLoadedEventListener l) {
	listenerList.add(IPageLoadedEventListener.class, l);
    }

    @Override
    public void removePageLoadedEventListener(final IPageLoadedEventListener l) {
	listenerList.remove(IPageLoadedEventListener.class, l);
    }

    protected final void fireDataLoadedEvent(final DataLoadedEvent e){
	for(final IDataLoadedEventListener listener : listenerList.getListeners(IDataLoadedEventListener.class)){
	    listener.dataLoaded(e);
	}
    }

    protected final void firePageLoadedEvent(final PageLoadedEvent e){
	for(final IPageLoadedEventListener listener : listenerList.getListeners(IPageLoadedEventListener.class)){
	    listener.pageLoaded(e);
	}
    }

}
