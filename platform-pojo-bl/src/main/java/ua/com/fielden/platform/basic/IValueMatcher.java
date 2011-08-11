package ua.com.fielden.platform.basic;

import java.util.List;

import ua.com.fielden.platform.equery.fetch;

/**
 * Custom implementation should be provided to be used in conjunction with the autocompleter component.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IValueMatcher<T> {
    /**
     * Returns query model used for fetching matching entities using method {@link #findMatchesWithModel(String)}.
     *
     */
    fetch<?> getFetchModel();

    /**
     * Define a way to specify query model used for fetching matching entities using method {@link #findMatchesWithModel(String)}.
     *
     * @param join
     *            -- join is used instead of an actual model in order to be able to introduce additional conditions ad hoc as part of method implementation.
     */
    void setFetchModel(final fetch<?> fetchModel);

    /**
     * Should provide matching logic using the passed value.
     * <p>
     * The returned entity instances adhere to the default entity model (i.e. only key and description are retrieved).
     *
     * @param value
     * @return
     */
    List<T> findMatches(final String value);

    /**
     * The same as {@link #findMatches(String)}, but uses the provided query model for initialising the matching entities. In cases where query model is not provided it behaves
     * exactly the same as {@link #findMatches(String)}.
     *
     * @param value
     * @return
     */
    List<T> findMatchesWithModel(final String value);

    /**
     * Returns a limit for size of entities returned by value matcher. Returns <code>null</code> if no limit is used.
     *
     * @return
     */
    Integer getPageSize();
}