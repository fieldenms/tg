package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the minimum height of an entity-bound component.
///
public interface IEntityComponentConfig3<T extends AbstractEntity<?>> extends IEntityComponentConfig4<T> {

    /// Specifies the minimum height of the component in pixels, which the component keeps whether it fills its layout cell or is resized by the user.
    /// It cannot be greater than the declared height.
    /// By default, the minimum height is that of the component itself, if any, such as 200 pixels for a map.
    ///
    IEntityComponentConfig4<T> withMinHeight(final int minHeight);

}
