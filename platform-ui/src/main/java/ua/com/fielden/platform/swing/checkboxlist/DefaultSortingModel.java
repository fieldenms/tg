package ua.com.fielden.platform.swing.checkboxlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SortOrder;
import javax.swing.event.EventListenerList;

public class DefaultSortingModel<T> implements ListSortingModel<T> {

    /** Event listener list. */
    private final EventListenerList listenerList = new EventListenerList();

    private final Set<T> sortableValues = new HashSet<T>();
    private final List<SortObject<T>> sortObjects = new ArrayList<SortObject<T>>();

    private boolean single = true;

    @Override
    public void setSortable(final T value, final boolean isSortable) {
	boolean result = false;
	if (isSortable) {
	    result = sortableValues.remove(value);
	} else {
	    result = sortableValues.add(value);
	}
	final SortObject<T> sortObject = getSortObject(value);
	if (result && sortObject != null && !isSortable) {
	    removeSortObject(sortObject, true);
	} else if (result) {
	    fireListSorterEvent(new SorterChangedEvent<T>(this, value, SortOrder.UNSORTED, null, -1, -1, !isSortable, isSortable));
	}

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

    protected void fireListSorterEvent(final EventObject event) {
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == SorterEventListener.class) {
		if (event instanceof SorterChangedEvent) {
		    ((SorterEventListener) listeners[i + 1]).valueChanged((SorterChangedEvent) event);
		} else if (event instanceof SortRangeChangedEvent) {
		    ((SorterEventListener) listeners[i + 1]).sortingRangeChanged((SortRangeChangedEvent) event);
		}
	    }
	}
    }

    @Override
    public void toggleSorter(final T value, final boolean discardPrevious) {
	SortObject<T> sortObject = getSortObject(value);
	if (isSingle() || discardPrevious) {
	    final Set<SortObject<T>> otherSortObjects = new HashSet<SortObject<T>>();
	    otherSortObjects.add(sortObject);
	    removeOtherSortObjects(otherSortObjects, false);
	}
	if (sortObject == null) {
	    sortObject = new SortObject<T>(value, SortOrder.ASCENDING);
	} else {
	    sortObject = sortObject.toggleSortObject();
	}
	addSortObject(sortObject);

    }

    private void removeOtherSortObjects(final Set<SortObject<T>> notRemoveSortObjects, final boolean sortableChanged) {
	final Set<SortObject<T>> notRemove = notRemoveSortObjects == null ? new HashSet<SortObject<T>>() : notRemoveSortObjects;
	for (final SortObject<T> sortObject : new ArrayList<SortObject<T>>(sortObjects)) {
	    if (!notRemove.contains(sortObject)) {
		removeSortObject(sortObject, sortableChanged);
	    }
	}
    }

    private void addSortObject(final SortObject<T> sortObject) {
	if (sortObject.getSortObject() == null) {
	    return;
	}
	final int previousSortObjectIndex = getSortObjectIndex(sortObject.getSortObject());
	final SortObject<T> previousSortObject = previousSortObjectIndex >= 0 ? sortObjects.get(previousSortObjectIndex) : null;
	if (previousSortObject != null) {
	    if (previousSortObject.getSortOrder() != sortObject.getSortOrder() && sortObject.getSortOrder() != null && SortOrder.UNSORTED != sortObject.getSortOrder()) {
		sortObjects.set(previousSortObjectIndex, sortObject);
		fireListSorterEvent(new SorterChangedEvent<T>(this, sortObject.getSortObject(), previousSortObject.getSortOrder(), sortObject.getSortOrder(), previousSortObjectIndex, previousSortObjectIndex, true, true));
	    } else if (sortObject.getSortOrder() == null || SortOrder.UNSORTED == sortObject.getSortOrder()) {
		removeSortObject(previousSortObject, false);
	    }
	} else {
	    final boolean isSortable = isSortable(sortObject.getSortObject());
	    if (isSortable && sortObject.getSortOrder() != null && SortOrder.UNSORTED != sortObject.getSortOrder()) {
		sortObjects.add(sortObject);
		fireListSorterEvent(new SorterChangedEvent<T>(this, sortObject.getSortObject(), SortOrder.UNSORTED, sortObject.getSortOrder(), -1, sortObjects.size() - 1, true, true));
	    }
	}
    }

    private void removeSortObject(final SortObject<T> sortObject, final boolean sortableChanged) {
	final int index = sortObjects.indexOf(sortObject);
	final boolean result = sortObjects.remove(sortObject);
	if (result) {
	    fireListSorterEvent(new SorterChangedEvent<T>(this, sortObject.getSortObject(), sortObject.getSortOrder(), sortableChanged ? null : SortOrder.UNSORTED, index, -1, true, sortableChanged ? false
		    : true));
	}
    }

    /**
     * Returns the index of the specified sort object in the sorting list.
     * 
     * @param value
     * @return
     */
    private int getSortObjectIndex(final T value) {
	for (int index = 0; index < sortObjects.size(); index++) {
	    final SortObject<T> sortObject = sortObjects.get(index);
	    if ((value == null && value == sortObject.getSortObject()) || (value != null && value.equals(sortObject.getSortObject()))) {
		return index;
	    }
	}
	return -1;
    }

    private SortObject<T> getSortObject(final T value) {
	final int index = getSortObjectIndex(value);
	if (index < 0) {
	    return null;
	}
	return sortObjects.get(index);
    }

    @Override
    public SortOrder getSortOrder(final T value) {
	final SortObject<T> sortObject = getSortObject(value);
	final boolean isSortable = isSortable(value);
	if (sortObject == null && isSortable) {
	    return SortOrder.UNSORTED;
	} else if (!isSortable) {
	    return null;
	}
	return sortObject.getSortOrder();
    }

    @Override
    public int getSortingOrder(final T value) {
	return getSortObjectIndex(value);
    }

    @Override
    public void setSortObjects(final List<SortObject<T>> sortObjects, final boolean discardPrevious) {
	if ((sortObjects == null || sortObjects.isEmpty()) && discardPrevious) {
	    removeOtherSortObjects(null, false);
	    return;
	}
	final Set<SortObject<T>> notRemove = new HashSet<SortObject<T>>();
	if (isSingle()) {
	    notRemove.add(getSortObject(sortObjects.get(sortObjects.size() - 1).getSortObject()));
	} else if (discardPrevious) {
	    for (final SortObject<T> sortObject : sortObjects) {
		notRemove.add(getSortObject(sortObject.getSortObject()));
	    }
	}
	if (isSingle() || discardPrevious) {
	    removeOtherSortObjects(notRemove, false);
	}
	for (final SortObject<T> sortObject : sortObjects) {
	    addSortObject(sortObject);
	}
    }

    @Override
    public List<SortObject<T>> getSortObjects() {
	return Collections.unmodifiableList(sortObjects);
    }

    @Override
    public boolean isSingle() {
	return single;
    }

    @Override
    public void setSingle(final boolean single) {
	if (this.single == single) {
	    return;
	}
	this.single = single;
	fireListSorterEvent(new SortRangeChangedEvent(this, single));
	if (single && !sortObjects.isEmpty()) {
	    final SortObject<T> sortObject = sortObjects.get(0);
	    final Set<SortObject<T>> notRemove = new HashSet<SortObject<T>>();
	    notRemove.add(sortObject);
	    removeOtherSortObjects(notRemove, false);
	}
    }

    @Override
    public void setSortable(final Set<T> sortableSet) {
	for (final T value : sortableSet) {
	    setSortable(value, false);
	}
    }

    @Override
    public Set<T> getSortableValues() {
	return Collections.unmodifiableSet(sortableValues);
    }
}
