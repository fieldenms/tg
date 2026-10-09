package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the element name of a component in place of an editor.
///
public interface IComponentConfig1<T extends AbstractEntity<?>> extends IComponentConfig2<T> {

    /// Specifies the name of the component element.
    /// By default, it is the last segment of the import path.
    ///
    IComponentConfig2<T> withElementName(final String elementName);

}
