package ua.com.fielden.platform.expression.editor;

import javax.swing.event.EventListenerList;

/**
 * This class implements basic logic of all property providers.
 * 
 * @author TG Team
 *
 */
public abstract class AbstractPropertyProvider implements IPropertyProvider {

    EventListenerList listenerList = new EventListenerList();

    @Override
    public void addPropertySelectionListener(final IPropertySelectionListener l) {
	listenerList.add(IPropertySelectionListener.class, l);
    }

    @Override
    public void removePropertySelectionListener(final IPropertySelectionListener l) {
	listenerList.remove(IPropertySelectionListener.class, l);
    }

    /**
     *Notify all listeners that have registered interest for notification on this event type. The event instance is lazily created using the parameters passed into the fire
     *method.
     */

    protected void fireSelectionPropertyEvent(final String propertyName, final boolean isSelect) {
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==IPropertySelectionListener.class) {
		// Lazily create the event:
		((IPropertySelectionListener)listeners[i+1]).propertyStateChanged(propertyName, isSelect);
	    }
	}
    }

}
