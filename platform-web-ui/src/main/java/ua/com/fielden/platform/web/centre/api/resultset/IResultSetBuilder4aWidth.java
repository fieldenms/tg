package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/// Provides the convenient way to specify rigid or flexible width for column in result set.
///
public interface IResultSetBuilder4aWidth<T extends AbstractEntity<?>> extends IResultSetBuilder4bWordWrap<T> {

    /// Specifies the rigid column width. The width of the column won't change when the size of grid changes.
    ///
    IResultSetBuilder4bWordWrap<T> width(int width);

    /// Specifies the flexible column width that will change when the width of grid changes.
    ///
    IResultSetBuilder4bWordWrap<T> minWidth(int minWidth);
}
