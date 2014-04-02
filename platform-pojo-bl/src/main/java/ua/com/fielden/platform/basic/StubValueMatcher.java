package ua.com.fielden.platform.basic;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 * A convenient very simplistic stub implementation, which can be used for creation of trivial value matchers that require to only implement method {@link #findMatches(String)}.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public abstract class StubValueMatcher<T> implements IValueMatcher<T> {

    @Override
    public List<T> findMatchesWithModel(final String value) {
        return findMatches(value);
    }

    @Override
    public <FT extends AbstractEntity<?>> fetch<FT> getFetchModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <FT extends AbstractEntity<?>> void setFetchModel(final fetch<FT> fetchModel) {
        // TODO Auto-generated method stub

    }

    @Override
    public Integer getPageSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public abstract List<T> findMatches(String value);
}
