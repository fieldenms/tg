package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentConfig0;

/// A configuration for an application-provided web component that represents a property in place of an editor.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master and to the name of the property.
///
public interface IComponentConfig<T extends AbstractEntity<?>> extends IComponentConfig0<T> {

    /// Specifies the name of the component element.
    /// By default, it is the last segment of the import path.
    ///
    IComponentConfig0<T> withElementName(final String elementName);

}
