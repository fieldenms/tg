package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the height of a component in place of an editor.
///
public interface IComponentConfig2<T extends AbstractEntity<?>> extends IComponentConfig3<T> {

    /// Specifies the height of the component in pixels, which makes the component resizable, as a rich text editor with a declared height,
    /// and which the component keeps at least.
    ///
    /// The user changes the height that the component keeps at least by dragging the resizer at its bottom right corner, and restores the declared height by double tapping the resizer.
    /// The height set by the user is remembered in the browser, for the user, the entity type of the master and the component.
    /// It is never less than the visible height of the component, so that the resizer shrinks a component in a flexible row only until the component fits the row.
    /// The component cannot be resized while it is blocked.
    ///
    /// The component has no height of its own, and fills the height of its layout cell.
    /// In a flexible row of the master layout, it grows with the master, and shrinks with the master down to the height that it keeps at least.
    /// In a row that takes the height of its content, it has the height that it keeps at least.
    /// By default, the component is not resizable, and keeps only its minimum height.
    ///
    IComponentConfig3<T> withHeight(final int height);

}
