package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

/// A contract for declaring actions of an entity-bound component, or completing its configuration.
///
public interface IEntityComponentConfig1<T extends AbstractEntity<?>> extends IAlso<T> {

    /// Declares an action that the component invokes itself, such as editing an entity it displays.
    /// Actions are made available to the component in the order of declaration.
    ///
    IEntityComponentConfig1<T> withAction(final EntityActionConfig action);

}
