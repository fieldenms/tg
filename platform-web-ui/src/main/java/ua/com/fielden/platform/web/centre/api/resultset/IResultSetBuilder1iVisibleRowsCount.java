package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/// Provides a convenient abstraction for specifying the number of visible rows in EGI.
///
public interface IResultSetBuilder1iVisibleRowsCount<T extends AbstractEntity<?>> extends IResultSetBuilder1jFitBehaviour<T> {

    /// Sets the number of rows that the egi shows, which the user can change in the centre configuration.
    ///
    IResultSetBuilder1jFitBehaviour<T> setVisibleRowsCount(int visibleRowsCount);

    /// Sets the height of the egi.
    /// The height can be anything that can be specified as the `height` CSS style of an element, such as `100%`, `640px`, `fit-content` or `100vh`.
    ///
    IResultSetBuilder1jFitBehaviour<T> setHeight(String height);
}
