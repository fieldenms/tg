package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to hide EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1aHideEgi<T extends AbstractEntity<?>> extends IResultSetBuilder1bCheckbox<T> {

    /**
     * Hides EGI.
     *
     * @return
     */
    IResultSetBuilder1bCheckbox<T> hideEgi();
}
