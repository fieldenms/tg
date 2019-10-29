package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to specify egi's row height
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1eRowHeight<T extends AbstractEntity<?>> extends IResultSetBuilder2Properties<T>{

    /**
     * Specifies egi's row height
     *
     * @return
     */
    IResultSetBuilder2Properties<T> rowHeight(String rowHeight);
}
