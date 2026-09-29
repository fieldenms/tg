package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the description of an entity-bound component.
///
public interface IEntityComponentConfig0<T extends AbstractEntity<?>> extends IEntityComponentConfig1<T> {

    /// Specifies the description of the component, which the component may display as a tooltip or a hint.
    /// Without a description, the component receives none.
    ///
    IEntityComponentConfig1<T> withDesc(final String desc);

}
