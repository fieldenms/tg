package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to specify styles for grid view icon.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1aEgiIconStyle<T extends AbstractEntity<?>> extends IResultSetBuilder1bCheckbox<T> {

    /**
     * Specifies style for grid view icon.
     *
     * @param icon
     * @return
     */
    IResultSetBuilder1bCheckbox<T> style(final String iconStyle);

}