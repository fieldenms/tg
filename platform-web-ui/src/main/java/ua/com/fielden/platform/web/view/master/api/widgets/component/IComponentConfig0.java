package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the description of a component in place of an editor.
///
public interface IComponentConfig0<T extends AbstractEntity<?>> extends IComponentConfig1<T> {

    /// Specifies the description of the component, which the component may display as a tooltip or a hint.
    /// By default, the component receives the description of its property.
    ///
    IComponentConfig1<T> withDesc(final String desc);

}
