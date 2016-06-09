package ua.com.fielden.platform.basic;

import java.util.List;

/**
 * A convenient very simplistic stub implementation, which can be used for creation of trivial value matchers that require to only implement method {@link #findMatches(String)}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class StubValueMatcher<T> implements IValueMatcher<T> {


    @Override
    public Integer getPageSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public abstract List<T> findMatches(String value);
}
