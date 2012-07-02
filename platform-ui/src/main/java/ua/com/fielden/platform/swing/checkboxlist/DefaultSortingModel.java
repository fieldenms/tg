package ua.com.fielden.platform.swing.checkboxlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

public class DefaultSortingModel<T> implements ListSortingModel<T> {

    /** Event listener list. */
    private final EventListenerList listenerList = new EventListenerList();

    private final Set<T> sortableValues = new HashSet<T>();
    private final List<Pair<T, Ordering>> sortObjects = new ArrayList<Pair<T, Ordering>>();

    @Override
    public void setItemUnsortable(final T value) {
	sortableValues.add(value);
    }

    @Override
    public boolean isSortable(final T value) {
	return !sortableValues.contains(value);
    }

    @Override
    public void addSorterEventListener(final SorterEventListener<T> listener) {
	listenerList.add(SorterEventListener.class, listener);
    }

    @Override
    public void removeSorterEventListener(final SorterEventListener<T> listener) {
	listenerList.remove(SorterEventListener.class, listener);
    }

    @SuppressWarnings("unchecked")
    protected void fireListSorterEvent(final SorterChangedEvent<T> event) {
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == SorterEventListener.class) {
		    ((SorterEventListener<T>) listeners[i + 1]).valueChanged(event);
	    }
	}
    }

    @Override
    public void toggleSorter(final T item) {
	    for (final Pair<T, Ordering> pair : sortObjects) {
		if (pair.getKey().equals(item)) {
		    final int index = sortObjects.indexOf(pair);
		    if (Ordering.ASCENDING.equals(pair.getValue())) {
			sortObjects.get(index).setValue(Ordering.DESCENDING);
		    } else { // Ordering.DESCENDING
			sortObjects.remove(index);
		    }
		   fireListSorterEvent(new SorterChangedEvent<T>(this, Collections.unmodifiableList(sortObjects), null));
		   return;
		}
	    } // if the property does not have an Ordering assigned -- put a ASC ordering to it (into the end of the list)
	    sortObjects.add(new Pair<T, Ordering>(item, Ordering.ASCENDING));

	    fireListSorterEvent(new SorterChangedEvent<T>(this, Collections.unmodifiableList(sortObjects), null));
    }

    @Override
    public List<Pair<T, Ordering>> getSortObjects() {
	return new ArrayList<Pair<T, Ordering>>(sortObjects);
    }
}
