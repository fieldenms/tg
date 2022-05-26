package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * The result set configuration contract that indicates whether all data should be retrieved at once or not.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1efRetrieveAll<T extends AbstractEntity<?>> extends IResultSetBuilder1fPageCapacity<T> {

    /**
     * Informs an entity centre that all matching data should be retrieved at once.
     * Data pagination is performed at the client side (aka virtual pagination).
     * <p>
     * This option should be used with care – only when it is necessary and the maximum amount of data can be estimated as not being "too excessive".
     * For example, if some data needs to be represented on a chart or a map, and it can be estimated that there would be at most {@code 3,000} data points, then configuring an entity centre to {@code retrieveAll} with virtual client-side pagination, would permit displaying the data on a chart or a map all at once.
     * <p>
     * It is important to distinguish the difference between "retrieving all" data and setting up a large page capacity:
     * <ul>
     * <li> "All" and some predefined large number are semantically different and in case of {@code pageCapacity + m (m > 1)}, data records would result in 2 and more pages, with only the data for the current page actually present at the client-side.
     * <li> Displaying large datasets on a single page leads to a performance hit in the current EGI implementation due to the need to stamp a complete DOM, required to display the all data at once.
     *      Virtual client-side pagination solves this limitation, where only the DOM for the current page is stamped, although all data is present at the client-side.
     *      The virtual pagination mode turns on automatically for the "retrieve all" option.
     * </ul>
     *
     * @return
     */
    IResultSetBuilder1fPageCapacity<T> retrieveAll();
}
