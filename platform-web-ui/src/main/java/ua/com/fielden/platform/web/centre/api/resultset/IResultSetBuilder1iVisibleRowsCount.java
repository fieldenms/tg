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

    /// Sets the number of rows that the egi shows, which the user can change in the centre configuration.
    ///
    /// It does not apply to the egi of a centre in a master component, which always fits to its height, as described in [IResultSetBuilder1jFitBehaviour#fitToHeight()].
    ///
    IResultSetBuilder1jFitBehaviour<T> setVisibleRowsCount(int visibleRowsCount);

    /// Set the height for egi.
    /// The height parameter could be anything that one can specify as height css style for element (e.g. '100%', 640px, 'fit-content', '100vh', etc.)
    ///
    /// It does not apply to the egi of a centre in a master component, which always fits to its height, as described in [IResultSetBuilder1jFitBehaviour#fitToHeight()].
    ///
    IResultSetBuilder1jFitBehaviour<T> setHeight(String height);
}
