package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * The result set configuration contract that indicates whether data all should be retrieved at once and then paginated on client side or not.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1efRetrieveAll<T extends AbstractEntity<?>> extends IResultSetBuilder1fPageCapacity<T> {

    /**
     * Retrieve all data at once to paginate on client
     *
     * @return
     */
    IResultSetBuilder1fPageCapacity<T> retrieveAll();
}
