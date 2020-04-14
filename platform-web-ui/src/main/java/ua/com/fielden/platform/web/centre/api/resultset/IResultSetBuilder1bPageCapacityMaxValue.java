package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Provides a convenient abstraction for specifying maximum page capacity for EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1bPageCapacityMaxValue<T extends AbstractEntity<?>> extends IResultSetBuilder1cHeaderWrap<T> {
    
    /**
     * Defines maximum value for result-set page capacity (aka max number of retrieved entities on page). If not set -- 300 is used.
     * 
     * @param maxPageCapacity
     * @return
     */
    IResultSetBuilder1cHeaderWrap<T> setMaxPageCapacity(final int maxPageCapacity);
    
}