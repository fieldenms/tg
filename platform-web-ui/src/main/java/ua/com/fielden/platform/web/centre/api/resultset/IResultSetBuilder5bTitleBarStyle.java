package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.layout.ICollapsedCardLayoutConfig;

/// A contract to style the EGI title bar.
/// It is available only after the title bar is specified with one of the `withTitleBar` methods of [IResultSetBuilder5aTitleBar].
///
/// Styling is optional.
///
public interface IResultSetBuilder5bTitleBarStyle<T extends AbstractEntity<?>> extends ICollapsedCardLayoutConfig<T> {

    /// Specifies the styles to be applied to the EGI title bar.
    /// They apply to the title bar only, not to the EGI.
    ///
    /// @param style CSS declarations, e.g. `"background-color: var(--paper-grey-700)"`; it must not be blank
    ///
    ICollapsedCardLayoutConfig<T> andStyle(final String style);

}
