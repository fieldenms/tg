package ua.com.fielden.platform.swing.checkboxlist;

import javax.swing.SortOrder;

/**
 * Associates object with it's {@link SortOrder}. Also provides automatic {@link SortOrder} cycling capability.
 * 
 * @author TG team
 * 
 * @param <T>
 */
public class SortObject<T> {

    private final T sortObject;
    private final SortOrder sortOrder;

    /**
     * Creates {@link SortObject} for specified {@code sortObject} and associated {@link SortOrder} instance.
     * 
     * @param sortObject
     * @param sortOrder
     */
    public SortObject(final T sortObject, final SortOrder sortOrder) {
	this.sortOrder = sortOrder;
	this.sortObject = sortObject;
    }

    /**
     * Returns next {@link SortOrder}.
     * 
     * @return
     */
    private SortOrder getNextOrder() {
	switch (sortOrder) {
	case ASCENDING:
	    return SortOrder.DESCENDING;
	case DESCENDING:
	    return SortOrder.UNSORTED;
	default:
	    return SortOrder.ASCENDING;
	}
    }

    /**
     * Returns {@link SortObject} with next {@link SortOrder} instance.
     * 
     * @return
     */
    public SortObject<T> toggleSortObject() {
	return new SortObject<T>(sortObject, getNextOrder());
    }

    /**
     * Returns sort object.
     * 
     * @return
     */
    public T getSortObject() {
	return sortObject;
    }

    /**
     * Returns {@link SortOrder} instance associated with specified sort object.
     * 
     * @return
     */
    public SortOrder getSortOrder() {
	return sortOrder;
    }

    /**
     * Returns the hash code for this {@link SortObject}.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
	int result = 17;
	result = 37 * result + (sortObject == null ? 0 : sortObject.hashCode());
	result = 37 * result + (sortOrder == null ? 0 : sortOrder.hashCode());
	return result;
    }

    /**
     * Returns true if this object equals the specified object. If the specified object is a {@link SortObject} and references the same sort object and sort order, the two objects
     * are equal.
     * 
     * @param o
     *            the object to compare to
     * @return true if <code>o</code> is equal to this {@link SortObject}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	}
	if (o == null || o.getClass() != this.getClass()) {
	    return false;
	}
	final SortObject<T> obj = (SortObject) o;
	if ((sortObject == null && sortObject != obj.sortObject) || (sortObject != null && !sortObject.equals(obj.sortObject))) {
	    return false;
	}
	return sortOrder == obj.sortOrder;
    }
}
