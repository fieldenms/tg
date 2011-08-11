package ua.com.fielden.platform.swing.checkboxlist;

import java.util.EventObject;

import javax.swing.SortOrder;

/**
 * An {@link EventObject} that is thrown when sort parameters of the specified sort object were changed
 * 
 * @author oleh
 * 
 * @param <T>
 */
public class SorterChangedEvent<T> extends EventObject {

    private static final long serialVersionUID = 4596731504844328063L;

    private final T sortObject;
    private final SortOrder oldSortOrder, newSortOrder;
    private final int oldSortingOrder, newSortingOrder;
    private final boolean oldSortable, newSortable;

    /**
     * Creates new {@link SortableChangeEvent} with specified sort object.
     * 
     * @param source
     * @param sortObject
     * @param propertyName
     *            - specifies the property that was changed.
     * @param sortValue
     *            - new value of the property specified with propertyName parameter.
     */
    public SorterChangedEvent(final Object source, final T sortObject, final SortOrder oldSortOrder, final SortOrder newSortOrder, final int oldSortingOrder, final int newSortingOrder, final boolean oldSortable, final boolean newSortable) {
	super(source);
	this.sortObject = sortObject;
	this.oldSortable = oldSortable;
	this.newSortable = newSortable;
	this.oldSortingOrder = oldSortingOrder;
	this.newSortingOrder = newSortingOrder;
	this.oldSortOrder = oldSortOrder;
	this.newSortOrder = newSortOrder;
    }

    /**
     * Returns sort object. Sort object - object for which sorting parameters were changed.
     * 
     * @return
     */
    public T getSortObject() {
	return sortObject;
    }

    public boolean isSortOrderChanged() {
	return getOldSortOrder() != getNewSortOrder();
    }

    public boolean isSortingOrderChanged() {
	return getOldSortingOrder() != getNewSortingOrder();
    }

    public boolean isSortableChanged() {
	return isOldSortable() != isNewSortable();
    }

    public SortOrder getOldSortOrder() {
	return oldSortOrder;
    }

    public SortOrder getNewSortOrder() {
	return newSortOrder;
    }

    public int getOldSortingOrder() {
	return oldSortingOrder;
    }

    public int getNewSortingOrder() {
	return newSortingOrder;
    }

    public boolean isOldSortable() {
	return oldSortable;
    }

    public boolean isNewSortable() {
	return newSortable;
    }

}
