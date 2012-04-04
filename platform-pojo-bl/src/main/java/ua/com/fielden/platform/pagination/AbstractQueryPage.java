package ua.com.fielden.platform.pagination;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Abstract convenience class for query pages, encapsulating pageNumber and pageCapacity properties and implementing related methods
 *
 * @author Yura, Oleh
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractQueryPage<T extends AbstractEntity<?>> implements IPage<T> {

    private final int pageNumber;
    private final int pageCapacity;

    public AbstractQueryPage(final int pageNumber, final int pageCapacity) {
	this.pageNumber = pageNumber;
	this.pageCapacity = pageCapacity;
    }

    @Override
    public boolean hasPrev() {
	return no() > 0;
    }

    @Override
    public int no() {
	return pageNumber;
    }

    @Override
    public int capacity() {
	return pageCapacity;
    }

}
