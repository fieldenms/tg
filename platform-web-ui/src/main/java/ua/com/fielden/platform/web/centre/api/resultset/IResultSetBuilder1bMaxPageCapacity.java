package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
*
* Provides a convenient abstraction for specifying the maximum page capacity for EGI.
*
* @author TG Team
*
* @param <T>
*/
public interface IResultSetBuilder1bMaxPageCapacity<T extends AbstractEntity<?>> extends IResultSetBuilder1bPageCapacity<T> {

    /**
     * Set the maximal number of entities on page.
     *
     * @param pageCapacity
     * @return
     */
    IResultSetBuilder1bPageCapacity<T> setMaxPageCapacity(int pageCapacity);
}
