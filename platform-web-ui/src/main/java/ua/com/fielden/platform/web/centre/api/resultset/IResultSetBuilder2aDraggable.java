package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * The result set configuration contract that allows one to make egi entities (e.a rows) draggable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder2aDraggable<T extends AbstractEntity<?>> extends IResultSetBuilder1bPageCapacity<T> {

    /**
     * Makes EGI rows draggable (by default it is false)
     *
     * @return
     */
    IResultSetBuilder1bPageCapacity<T>  draggable();
}
