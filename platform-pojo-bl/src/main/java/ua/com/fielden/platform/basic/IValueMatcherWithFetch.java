package ua.com.fielden.platform.basic;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 * A contract for value matcher with custom fetch strategy.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IValueMatcherWithFetch<T extends AbstractEntity<?>> extends IValueMatcher<T> {

    /**
     * Return the provided custom fetch strategy for entity retrieval.
     *
     */
    fetch<T> getFetch();

    /**
     * Sets a custom fetch strategy for entity retrieval.
     */
    void setFetch(final fetch<T> fetchModel);

    /**
     * The same as {@link #findMatches(String)}, but uses a the provided custom fetch strategy when retrieving entities.
     *
     * @param value
     * @return
     */
    List<T> findMatchesWithModel(final String value);

}
