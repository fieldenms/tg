package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentConfig0;

/// A configuration for an application-provided web component in an entity master,
/// which represents a property in place of an editor, declared with `asComponent`,
/// or is bound to the entity of the master rather than to any of its properties, declared with `addComponent` and placed in the master layout next to the editors.
///
/// The component is read-only.
/// The component receives the fully-fledged entity of the master in `entity`, rather than the binding entity of editors,
/// and a component in place of an editor also receives the name of its property in `property-name`.
/// Both forms are configured with the same declarations, whose defaults differ where the form matters, as described for each declaration.
///
public interface IComponentConfig<T extends AbstractEntity<?>> extends IComponentConfig0<T> {

    /// Specifies the title of the component, which the component may display as its caption.
    /// By default, a component in place of an editor receives the title of its property, and a component bound to the entity receives none.
    ///
    IComponentConfig0<T> withTitle(final String title);

}
