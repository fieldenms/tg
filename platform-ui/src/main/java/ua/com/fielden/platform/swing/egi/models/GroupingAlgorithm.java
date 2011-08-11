package ua.com.fielden.platform.swing.egi.models;

import java.util.List;

/**
 * Represents abstract algorithm for grouping elements of a list.
 * 
 * @author Yura
 */
public interface GroupingAlgorithm<T> {

    /**
     * Should return list of element groups.
     * 
     * @param elements
     * @return
     */
    List<List<T>> group(List<T> elements);

}
