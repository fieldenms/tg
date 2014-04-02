package ua.com.fielden.platform.swing.review.development;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadNotifier;

public class DefaultLoadNotifier implements ILoadNotifier {

    private final EventListenerList listenerList = new EventListenerList();

    @Override
    public void addLoadListener(final ILoadListener listener) {
        listenerList.add(ILoadListener.class, listener);
    }

    @Override
    public void removeLoadListener(final ILoadListener listener) {
        listenerList.add(ILoadListener.class, listener);
    }

    public void fireLoadEvent(final LoadEvent event) {
        for (final ILoadListener listener : listenerList.getListeners(ILoadListener.class)) {
            listener.viewWasLoaded(event);
        }
    }

}
