package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IEntityComponentConfig0;

/// A configuration for an application-provided web component that is bound to the entity of a master rather than to any of its properties.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master, and is placed in the master layout next to the editors.
///
public interface IEntityComponentConfig<T extends AbstractEntity<?>> extends IEntityComponentConfig0<T> {

    /// Specifies the name of the component element.
    /// By default, it is the last segment of the import path.
    ///
    IEntityComponentConfig0<T> withElementName(final String elementName);

}
