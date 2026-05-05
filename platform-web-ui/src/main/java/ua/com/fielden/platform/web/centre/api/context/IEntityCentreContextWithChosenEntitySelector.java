package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

/// Entry-level extension of [IEntityCentreContextSelector] that exposes the `withChosenEntity()` opt-in for populating `chosenEntity` on the resulting [CentreContextConfig].
///
/// The opt-in is only available at the start of the fluent chain.
/// Once any of the other `with*` steps has been invoked, the chain advances into a numbered selector that does not expose `withChosenEntity()`.
/// Calling the method returns [IEntityCentreContextSelectorAfterChosenEntity], which makes the opt-in idempotent at the type level — it cannot be invoked twice in the same chain.
///
/// Example usage:
///
/// ```java
/// context().withChosenEntity().withCurrentEntity().withSelectionCrit().build();   // chosen entity is included
/// context().withCurrentEntity().build();                                          // unchanged behaviour, no chosen entity
/// ```
///
public interface IEntityCentreContextWithChosenEntitySelector<T extends AbstractEntity<?>> extends IEntityCentreContextSelector<T> {

    /// Opts in to populate `chosenEntity` on the resulting [CentreContextConfig].
    /// The flag is threaded through every subsequent transition in the fluent chain.
    /// Must be the first step after `context()` — the returned [IEntityCentreContextSelectorAfterChosenEntity] no longer exposes this method,
    /// but does allow either continuing with one of the standard `with*` steps or finishing immediately via `build()` for a chosen-entity-only configuration.
    ///
    IEntityCentreContextSelectorAfterChosenEntity<T> withChosenEntity();
}
