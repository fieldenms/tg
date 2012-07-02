package ua.com.fielden.platform.swing.checkboxlist;

import java.util.EventObject;
import java.util.List;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

/**
 * An {@link EventObject} that is thrown when sort parameters of the specified sort object were changed
 *
 * @author oleh
 *
 * @param <T>
 */
public class SorterChangedEvent<T> extends EventObject {

    private static final long serialVersionUID = 4596731504844328063L;

    private final List<Pair<T, Ordering>> newSortObjectes;
    private final List<Pair<T, Ordering>> oldSortObjectes;


    /**
     * Creates new {@link SortableChangeEvent} with specified sort object.
     *
     * @param source
     * @param newSortObjectes
     * @param oldSortObjectes
     */
    public SorterChangedEvent(final Object source, final List<Pair<T, Ordering>> newSortObjectes, final List<Pair<T, Ordering>> oldSortObjectes) {
	super(source);
	this.newSortObjectes = newSortObjectes;
	this.oldSortObjectes = oldSortObjectes;
    }

    /**
     * Returns new sort values. The ordered list of pairs (value and it's ordering).
     *
     * @return
     */
    public List<Pair<T, Ordering>> getNewSortObjectes() {
	return newSortObjectes;
    }

    /**
     * Returns old sort values. The ordered list of pairs (value and it's ordering).
     *
     * @return
     */
    public List<Pair<T, Ordering>> getOldSortObjectes() {
	return oldSortObjectes;
    }

}
