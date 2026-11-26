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
    /// This is the title of the nearest menu item that contains the centre:
    /// - for a centre in a section of a compound master, the title of the compound master's menu item for that section (e.g., "Tasks");
    /// - otherwise, the title of the main menu item (e.g., "Work Board"), also for a centre embedded in an insertion point of the centre in that menu item.
    ///
    /// A centre in a dialog takes the title from where the dialog was opened.
    /// Unlike the title of the page, the title bar shows only the menu item title, without the name of the centre configuration (save-as name).
    ///
    IResultSetBuilder5bTitleBarStyle<T> withTitleBar();

}
