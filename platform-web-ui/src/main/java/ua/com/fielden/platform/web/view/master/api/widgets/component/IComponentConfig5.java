package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for declaring the condition for refreshing a component after a validation of the master entity.
///
public interface IComponentConfig5<T extends AbstractEntity<?>> extends IComponentConfig6<T> {

    /// Declares the condition for refreshing the component after a validation of the master entity.
    ///
    /// The master refreshes the component whenever it retrieves the entity, which happens when the master is opened and when its changes are cancelled, and whenever it saves the entity.
    /// With a condition, the component is also refreshed after each validation of a change made by the user for which the condition holds, as described in [IComponentRefreshCondition].
    ///
    IComponentConfig6<T> refreshWhen(final Class<? extends IComponentRefreshCondition<T>> condition);

}
