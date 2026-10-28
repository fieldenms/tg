package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A condition for refreshing a component in an entity master after a validation of the master entity, declared for the component with `refreshWhen` in the Entity Master DSL.
///
/// The master refreshes its components whenever it retrieves the entity, which happens when the master is opened and when its changes are cancelled, and whenever it saves the entity.
/// Between these, the master validates each change made by the user, and a component with a condition is also refreshed after each validation for which the condition holds.
/// Without a condition, a component bound to the entity is not refreshed after a validation,
/// while a component in place of an editor is refreshed whenever the value of its property changes, which a condition replaces.
/// What refreshing does is up to the component, such as running an embedded centre again.
///
/// The condition is created with the injector, as the selector of a multi-action is, so that its dependencies are injected.
///
/// @param <T> the entity type of the master
///
public interface IComponentRefreshCondition<T extends AbstractEntity<?>> {

    /// Determines whether the component should be refreshed after the validation described by `context`,
    /// which provides the validated entity and the properties whose values the user changed.
    ///
    boolean shouldRefresh(final ComponentRefreshContext<T> context);

}
