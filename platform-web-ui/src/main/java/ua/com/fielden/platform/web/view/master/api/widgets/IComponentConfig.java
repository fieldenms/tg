package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentConfig0;

/// A configuration for an application-provided web component that represents a property in place of an editor.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master and to the name of the property.
///
public interface IComponentConfig<T extends AbstractEntity<?>> extends IComponentConfig0<T> {

    /// Specifies the title of the component, which the component may display as its caption.
    /// By default, the component receives the title of its property.
    ///
    IComponentConfig0<T> withTitle(final String title);

}
