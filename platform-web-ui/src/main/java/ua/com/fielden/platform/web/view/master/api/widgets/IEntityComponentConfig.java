package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IEntityComponentConfig0;

/// A configuration for an application-provided web component that is bound to the entity of a master rather than to any of its properties.
///
/// The component is read-only.
/// It is bound to the fully-fledged entity of the master, and is placed in the master layout next to the editors.
///
public interface IEntityComponentConfig<T extends AbstractEntity<?>> extends IEntityComponentConfig0<T> {

    /// Specifies the title of the component, which the component may display as its caption.
    /// Without a title, the component receives none.
    ///
    IEntityComponentConfig0<T> withTitle(final String title);

}
