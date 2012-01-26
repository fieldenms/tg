package ua.com.fielden.platform.swing.review.report.analysis.chart;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedListener;

public abstract class AbstractCategoryAnalysisDataProvider<CDT, ADT, LDT> implements ICategoryAnalysisDataProvider<CDT, ADT, LDT> {

    private final EventListenerList listenerList = new EventListenerList();

    @Override
    public void addAnalysisModelChangedListener(final AnalysisModelChangedListener l) {
	this.listenerList.add(AnalysisModelChangedListener.class, l);
    }

    @Override
    public void removeAnalysisModelChangedListener(final AnalysisModelChangedListener l) {
	this.listenerList.remove(AnalysisModelChangedListener.class, l);
    }

    /**
     * Notifies all {@link AnalysisModelChangedListener}s that the data of this model was changed.
     * 
     * @param event
     */
    protected final void fireAnalysisModelChangeEvent(final AnalysisModelChangedEvent event) {
	for(final AnalysisModelChangedListener listener : listenerList.getListeners(AnalysisModelChangedListener.class)){
	    listener.cahrtModelChanged(event);
	}
    }

}
