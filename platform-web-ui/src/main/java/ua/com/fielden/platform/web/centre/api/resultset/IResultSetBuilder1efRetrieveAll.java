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
     * Retrieve all data at once to paginate on client
     *
     * @return
     */
    IResultSetBuilder1fPageCapacity<T> retrieveAll();
}
