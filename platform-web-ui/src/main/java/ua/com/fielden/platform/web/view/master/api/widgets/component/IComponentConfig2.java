package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the height of a component in place of an editor.
///
public interface IComponentConfig2<T extends AbstractEntity<?>> extends IComponentConfig3<T> {

    /// Specifies the height of the component in pixels, which makes the component resizable, as a rich text editor with a declared height.
    ///
    /// The user resizes the component by dragging the resizer at its bottom right corner, and restores the declared height by double tapping the resizer.
    /// The height set by the user is remembered in the browser, for the user, the entity type of the master and the component.
    /// The component cannot be resized while it is blocked.
    ///
    /// By default, the component has no height of its own, and fills the height of its layout cell, such as in a flexible row of the master layout.
    /// A component with a declared height should be laid out in a row that takes the height of its content instead.
    ///
    IComponentConfig3<T> withHeight(final int height);

}
