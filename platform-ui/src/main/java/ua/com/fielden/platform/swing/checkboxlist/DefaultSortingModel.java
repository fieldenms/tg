package ua.com.fielden.platform.swing.checkboxlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
	toggleSortingItem(item, false);
    }

    @Override
    public void toggleSorterSingle(final T item) {
	toggleSortingItem(item, true);
    }

    @Override
    public List<Pair<T, Ordering>> getSortObjects() {
	return new ArrayList<Pair<T, Ordering>>(sortObjects);
    }

    /**
     * Toggles the {@link Ordering} for the specified item (multiple or single)
     *
     * @param item
     * @param single
     */
    private void toggleSortingItem(final T item, final boolean single){
	final Iterator<Pair<T, Ordering>> iterator = sortObjects.iterator();
	boolean wasToggled = false;
	while(iterator.hasNext()){
	    final Pair<T, Ordering> pair = iterator.next();
	    if (!pair.getKey().equals(item)) {
		if (single) {
		    iterator.remove();
		}
	    }else{
		if (Ordering.ASCENDING.equals(pair.getValue())) {
		    pair.setValue(Ordering.DESCENDING);
		} else { // Ordering.DESCENDING
		    iterator.remove();
		}
		wasToggled = true;
	    }
	}
	if(!wasToggled){
	    sortObjects.add(new Pair<T, Ordering>(item, Ordering.ASCENDING));
	}
	fireListSorterEvent(new SorterChangedEvent<T>(this, Collections.unmodifiableList(sortObjects), null));
    }
}
