package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to define EGI appearance: to hide it or specify icon for it.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1aEgiAppearance<T extends AbstractEntity<?>> extends IResultSetBuilder1bCheckbox<T> {

    /**
     * Hides EGI.
     *
     * @return
     */
    IResultSetBuilder1bCheckbox<T> hideEgi();

    /**
     * Specifies icon for grid view to be shown in centre switch view button.
     *
     * @return
     */
    IResultSetBuilder1aEgiIconStyle<T> withGridViewIcon(String icon);
}
