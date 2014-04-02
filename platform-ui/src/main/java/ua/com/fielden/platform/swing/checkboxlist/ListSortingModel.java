package ua.com.fielden.platform.swing.checkboxlist;

import java.util.List;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

/**
 * Model that handles sorting for check box list.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface ListSortingModel<T> {

    /**
     * Toggles {@link Ordering} for specified item.
     * 
     * @param item
     */
    void toggleSorter(T item);

    /**
     * Toggles {@link Ordering} for specified item and resets others.
     * 
     * @param item
     */
    void toggleSorterSingle(T item);

    /**
     * Returns the list of ordered items with their {@link Ordering}s.
     * 
     * @return
     */
    List<Pair<T, Ordering>> getSortObjects();

    /**
     * Sets the specified item sortable or unsortable.
     * 
     * @param item
     * @param sortable
     */
    void setItemUnsortable(T item);

    /**
     * Returns value that indicates whether specified item is sortable or not.
     * 
     * @param item
     * @return
     */
    boolean isSortable(T item);

    /**
     * Registers the {@link SorterEventListener} instance, that listens the sorting change events.
     * 
     * @param listener
     */
    void addSorterEventListener(SorterEventListener<T> listener);

    /**
     * Unregisters the {@link SorterEventListener} instance.
     * 
     * @param listener
     */
    void removeSorterEventListener(SorterEventListener<T> listener);
}
