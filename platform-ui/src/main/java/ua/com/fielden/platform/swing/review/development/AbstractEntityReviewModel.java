package ua.com.fielden.platform.swing.review.development;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;

public abstract class AbstractEntityReviewModel<T extends AbstractEntity, DTM extends IDomainTreeManager> {

    private final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria;

    //private final EventListenerList listenerList;

    public AbstractEntityReviewModel(final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria){
	//this.listenerList = new EventListenerList();
	this.criteria = criteria;
    }

    public EntityQueryCriteria<DTM, T, IEntityDao<T>> getCriteria() {
	return criteria;
    }

    //    public void addDataLoadedEventListener(final IDataLoadedEventListener l) {
    //	listenerList.add(IDataLoadedEventListener.class, l);
    //    }
    //
    //    public void removeDataLoadedEventListener(final IDataLoadedEventListener l) {
    //	listenerList.remove(IDataLoadedEventListener.class, l);
    //    }
    //
    //    public void addPageLoadedEventListener(final IPageLoadedEventListener l) {
    //	listenerList.add(IPageLoadedEventListener.class, l);
    //    }
    //
    //    public void removePageLoadedEventListener(final IPageLoadedEventListener l) {
    //	listenerList.remove(IPageLoadedEventListener.class, l);
    //    }
    //
    //    protected final void fireDataLoadedEvent(final DataLoadedEvent e){
    //	for(final IDataLoadedEventListener listener : listenerList.getListeners(IDataLoadedEventListener.class)){
    //	    listener.dataLoaded(e);
    //	}
    //    }
    //
    //    protected final void firePageLoadedEvent(final PageLoadedEvent e){
    //	for(final IPageLoadedEventListener listener : listenerList.getListeners(IPageLoadedEventListener.class)){
    //	    listener.pageLoaded(e);
    //	}
    //    }

}
