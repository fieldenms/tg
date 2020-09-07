package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Provides a convenient abstraction for specifying the page capacity for EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1fPageCapacity<T extends AbstractEntity<?>> extends IResultSetBuilder1gMaxPageCapacity<T> {
    
    /**
     * Defines result-set page capacity (aka max number of retrieved entities on page). If not set -- 30 is used.
     * 
     * @param pageCapacity
     * @return
     */
    IResultSetBuilder1gMaxPageCapacity<T> setPageCapacity(final int pageCapacity);
    
}