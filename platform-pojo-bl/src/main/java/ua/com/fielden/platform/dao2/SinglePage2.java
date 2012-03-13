package ua.com.fielden.platform.dao2;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.pagination.IPage2;

/**
 * An abstraction of the list of entities as a single page.
 *
 */
public class SinglePage2<T extends AbstractEntity<?>> implements IPage2<T> {
    private final List<T> data;

    public SinglePage2(final List<T> data) {
	this.data = new ArrayList<T>(data);
    }

    @Override
    public EntityAggregates summary() {
	return null;
    }

    @Override
    public int capacity() {
	return data.size();
    }

    @Override
    public List<T> data() {
	return data;
    }

    @Override
    public boolean hasNext() {
	return false;
    }

    @Override
    public boolean hasPrev() {
	return false;
    }

    @Override
    public IPage2<T> next() {
	return this;
    }

    @Override
    public IPage2<T> prev() {
	return this;
    }

    @Override
    public IPage2<T> first() {
	return this;
    }

    @Override
    public IPage2<T> last() {
	return this;
    }

    @Override
    public int numberOfPages() {
	return 1;
    }

    @Override
    public String toString() {
	return "Single page";
    }

    @Override
    public int no() {
	return 0;
    }
}