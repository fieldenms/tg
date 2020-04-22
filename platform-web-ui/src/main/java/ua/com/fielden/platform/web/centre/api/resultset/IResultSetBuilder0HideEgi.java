package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to hide EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder0HideEgi<T extends AbstractEntity<?>> extends IResultSetBuilder0Checkbox<T> {

    /**
     * Hides EGI.
     *
     * @return
     */
    IResultSetBuilder0Checkbox<T> hideEgi();
}
