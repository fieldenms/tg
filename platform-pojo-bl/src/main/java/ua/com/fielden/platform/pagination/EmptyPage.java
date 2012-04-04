/**
 *
 */
package ua.com.fielden.platform.pagination;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;

/**
 * This class represents empty page list
 *
 * @author Yura, Oleh
 */
public class EmptyPage<T extends AbstractEntity<?>> implements IPage<T> {

    @Override
    public EntityAggregates summary() {
	return null;
    }

    @Override
    public int capacity() {
	return 0;
    }

    @Override
    public List<T> data() {
	return new ArrayList<T>();
    }

    @Override
    public IPage<T> first() {
	return this;
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
    public IPage<T> last() {
	return this;
    }

    @Override
    public IPage<T> next() {
	return null;
    }

    @Override
    public int no() {
	return 0;
    }

    @Override
    public IPage<T> prev() {
	return null;
    }

    @Override
    public int numberOfPages() {
	return 1;
    }

    @Override
    public String toString() {
	return "1 of 1";
    }

}
