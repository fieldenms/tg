package ua.com.fielden.platform.pagination;

import java.util.Iterator;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A convenient abstraction that wraps <code>IPage</code> instance in order to make it iterable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class IterablePage<T extends AbstractEntity<?>> implements Iterable<T>, Iterator<T> {

    /** References the current page being iterated over.
     * It gets replaced with the next page if that needs to be loaded in the process of iteration.
     */
    private IPage<T> page;
    /**
     * Represents the index of the page entry currently being pointed by this iterator.
     */
    private int index;

    public IterablePage(final IPage<T> page) {
	this.page = page;
	this.index = 0;
    }

    @Override
    public boolean hasNext() {
	return index < page.data().size() || page.hasNext();
    }

    @Override
    public T next() {
	final int pageSize = page.data().size();
	if (index < pageSize) {
	    return page.data().get(index++);
	} else {
	    page = page.next();
	    index = 0;
	    return page.data().get(index++);
	}
    }

    @Override
    public void remove() {
	throw new UnsupportedOperationException("Page is immutalbe.");
    }

    @Override
    public Iterator<T> iterator() {
	return this;
    }

}
