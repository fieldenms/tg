package ua.com.fielden.platform.basic;

import java.util.List;

/**
 * The most general contract to be used for defining custom autocompletion logic.
 * It is suitable for autocompleting entitites, enums, POJOs... anything really as the type parameter <code>T</code> does not have an upper bound.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IValueMatcher<T> {

    /**
     * Should provide matching logic using the passed value.
     *
     * @param value
     * @return
     */
    List<T> findMatches(final String value);

    /**
     * Defines the maximum number of values to be presented during autocompletion.
     *
     * @return
     */
    Integer getPageSize();
}