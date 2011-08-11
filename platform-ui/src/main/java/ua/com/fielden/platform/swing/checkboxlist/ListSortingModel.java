package ua.com.fielden.platform.swing.checkboxlist;

import java.util.List;
import java.util.Set;

import javax.swing.SortOrder;

public interface ListSortingModel<T> {

    void setSortable(T value, boolean isSortable);

    boolean isSortable(T value);

    void toggleSorter(T value, boolean discardPrevious);

    SortOrder getSortOrder(T value);

    int getSortingOrder(T value);

    void setSortObjects(List<SortObject<T>> sortObjects, boolean discardPrevious);

    List<SortObject<T>> getSortObjects();

    void setSortable(Set<T> sortableMap);

    Set<T> getSortableValues();

    void addSorterEventListener(SorterEventListener<T> listener);

    void removeSorterEventListener(SorterEventListener<T> listener);

    boolean isSingle();

    void setSingle(boolean single);
}
