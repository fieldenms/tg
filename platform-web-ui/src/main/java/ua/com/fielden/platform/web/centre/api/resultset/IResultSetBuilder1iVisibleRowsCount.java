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
public interface IResultSetBuilder1iVisibleRowsCount<T extends AbstractEntity<?>> extends IResultSetBuilder1jFitBehaviour<T> {

    IResultSetBuilder1jFitBehaviour<T> setVisibleRowsCount(int visibleRowsCount);

    /**
     * Set the height for egi. The height parameter could be anything that one can specify as height css style for element (e.g. '100%', 640px, 'fit-content', '100vh', etc.)
     *
     * @param height
     * @return
     */
    IResultSetBuilder1jFitBehaviour<T> setHeight(String height);
}
