package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring attributes of a component element.
///
public interface IComponentConfig0<T extends AbstractEntity<?>> extends IComponentConfig1<T> {

    /// Declares an attribute of the component element.
    ///
    /// The name is in dash-case, as it appears in HTML, such as `centre-uuid`.
    /// The value is either static, such as `compact`, or a pass-through binding expression over the master, such as `[[centreUuid]]`.
    /// Attributes are rendered in the order of declaration.
    ///
    IComponentConfig0<T> withAttr(final String name, final CharSequence value);

}
