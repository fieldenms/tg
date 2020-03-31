package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * Provides a convenient abstraction for specifying the page capacity for EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1bPageCapacity<T extends AbstractEntity<?>> extends IResultSetBuilder1cMaxHeaderLines<T> {

    IResultSetBuilder1cMaxHeaderLines<T> setPageCapacity(int pageCapacity);

}
