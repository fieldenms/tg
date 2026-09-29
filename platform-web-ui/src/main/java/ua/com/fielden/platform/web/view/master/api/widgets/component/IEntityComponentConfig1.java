package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the element name of an entity-bound component.
///
public interface IEntityComponentConfig1<T extends AbstractEntity<?>> extends IEntityComponentConfig2<T> {

    /// Specifies the name of the component element.
    /// By default, it is the last segment of the import path.
    ///
    IEntityComponentConfig2<T> withElementName(final String elementName);

}
