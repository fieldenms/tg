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
/// A refresh asked for while the component is blocked takes place once it is unblocked.
/// A component that is blocked while the entity is unsaved, as components are by default, is blocked whenever the user has changed a persisted entity,
/// and is unblocked when the changes are saved or cancelled, which refreshes all components anyway.
/// So a condition takes effect for components declared with `skipBlockingWhenUnsaved`, such as an embedded centre that follows a property as the user changes it,
/// and in masters of entity types that are not persistent.
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
