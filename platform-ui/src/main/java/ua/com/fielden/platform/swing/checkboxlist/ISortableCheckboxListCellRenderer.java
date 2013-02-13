package ua.com.fielden.platform.swing.checkboxlist;

/**
 * Cell renderer contract for {@link SortingCheckboxList}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISortableCheckboxListCellRenderer<T> {

    /**
     * Returns an index of the checking component that contains point specified with x and y coordinates.
     *
     * @param x - the x coordinate of the point.
     * @param y - the y coordinate of the point.
     * @return
     */
    int getHotSpot(int x, int y);

    /**
     * Returns the value that indicates whether the point, specified with x and y coordinates, is on the ordering arrow.
     *
     * @param x
     * @param y
     * @return
     */
    boolean isOnOrderingArrow(int x, int y);

    /**
     * Returns the value that indicates whether sorting arrow is visible for the specified element.
     *
     * @param element
     * @return
     */
    boolean isSortingAvailable(T element);
}
