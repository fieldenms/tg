package ua.com.fielden.platform.reportquery;

import javax.swing.event.EventListenerList;

public abstract class AbstractCategoryChartEntryModel implements ICategoryChartEntryModel {

    private final EventListenerList listenerList;

    public AbstractCategoryChartEntryModel(){
	this.listenerList = new EventListenerList();
    }

    @Override
    public void addAnalysisModelChangedListener(final AnalysisModelChangedListener l) {
	listenerList.add(AnalysisModelChangedListener.class, l);
    }

    @Override
    public void removeAnalysisModelChangedListener(final AnalysisModelChangedListener l) {
	listenerList.add(AnalysisModelChangedListener.class, l);
    }

    protected void notifyChartModelChanged(final AnalysisModelChangedEvent event){
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==AnalysisModelChangedListener.class) {
		((AnalysisModelChangedListener)listeners[i+1]).cahrtModelChanged(event);
	    }
	}

    }

}
