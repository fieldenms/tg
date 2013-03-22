package ua.com.fielden.platform.pagination;

import java.util.Iterator;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A convenient abstraction that wraps <code>IPage</code> instance in order to make it iterable.
 * <p>
 * <b>IMPORTANT:</b> data used in the underlying pages should not be removed or mutated in a way that when calling <code>currentPage.next</code> it could return an empty page!
 * <p/>
 * As an example of this, consider db-aware implementation of {@link IPage} that is used for implementing pagination over a result set where the underlying SQL is
 * constructed using the <code>offset</code> operator.
 * Let's also consider that the original query identified two pages with two elements each, and our task is to iterated over all entries in those two pages to perform some operation to each entry.
 * If that operation mutates the data in a way that it does not match the original selection criteria then after finishing with the first page,
 * calling the next page would produce an empty page whereas in fact there are those two remaining records that still need to be processed.
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
