package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.layout.ICollapsedCardLayoutConfig;

/// A contract to show the EGI of an entity centre with a title bar.
/// It is available right after all properties of the EGI are specified.
///
/// The title bar is optional: without calling either of the `withTitleBar` methods, the EGI is shown without it.
///
public interface IResultSetBuilder5aTitleBar<T extends AbstractEntity<?>> extends ICollapsedCardLayoutConfig<T> {

    /// Shows the EGI with a title bar, which displays the specified title.
    ///
    /// @param title the title to display; it must not be blank
    ///
    IResultSetBuilder5bTitleBarStyle<T> withTitleBar(final String title);

    /// Shows the EGI with a title bar, which displays the same title as the entity centre has.
    ///
    IResultSetBuilder5bTitleBarStyle<T> withTitleBar();

}
