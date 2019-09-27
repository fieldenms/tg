package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This contract provides a way to specify that editor in egi is editable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilderEditable<T extends AbstractEntity<?>> extends IResultSetBuilder3Ordering<T> {


    /**
     * Makes egi property editable
     *
     * @return
     */
    IResultSetBuilder3Ordering<T> editable();
}
