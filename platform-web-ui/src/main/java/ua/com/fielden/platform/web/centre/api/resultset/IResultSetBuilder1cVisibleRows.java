package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * Provides a convenient abstraction for specifying the number of visible rows in EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1cVisibleRows<T extends AbstractEntity<?>> extends IResultSetBuilder2Properties<T> {

    IResultSetBuilder2Properties<T> setVisibleRowsCount(int visibleRowsCount);
}
