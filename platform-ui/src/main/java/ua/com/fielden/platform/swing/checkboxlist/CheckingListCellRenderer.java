package ua.com.fielden.platform.swing.checkboxlist;

import javax.swing.ListCellRenderer;

/**
 * Cell renderer for {@link CheckboxList}.
 * 
 * @author oleh
 * 
 */
public interface CheckingListCellRenderer<T> extends ListCellRenderer {

    /**
     * Returns value that indicates whether point specified with x and y coordinates is on the checking component or not.
     * 
     * @param x
     * @param y
     * @return
     */
    boolean isOnHotSpot(int x, int y);
}
