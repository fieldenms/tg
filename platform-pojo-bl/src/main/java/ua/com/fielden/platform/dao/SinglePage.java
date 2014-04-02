package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

/**
 * An abstraction of the list of entities as a single page.
 * 
 * @author TG Team
 */
public class SinglePage<T extends AbstractEntity<?>> implements IPage<T> {
    private final List<T> data;

    public SinglePage(final List<T> data) {
        this.data = new ArrayList<T>(data);
    }

    @Override
    public T summary() {
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
    public IPage<T> next() {
        return this;
    }

    @Override
    public IPage<T> prev() {
        return this;
    }

    @Override
    public IPage<T> first() {
        return this;
    }

    @Override
    public IPage<T> last() {
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