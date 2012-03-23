package ua.com.fielden.platform.swing.review.development;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;

public abstract class AbstractEntityReviewModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> {

    private final EntityQueryCriteria<CDTME, T, IEntityDao2<T>> criteria;

    private final AbstractConfigurationModel configurationModel;

    //private final EventListenerList listenerList;

    public AbstractEntityReviewModel(final AbstractConfigurationModel configurationModel, final EntityQueryCriteria<CDTME, T, IEntityDao2<T>> criteria){
	//this.listenerList = new EventListenerList();
	this.configurationModel = configurationModel;
	this.criteria = criteria;
    }

    public EntityQueryCriteria<CDTME, T, IEntityDao2<T>> getCriteria() {
	return criteria;
    }

    public AbstractConfigurationModel getConfigurationModel() {
	return configurationModel;
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
