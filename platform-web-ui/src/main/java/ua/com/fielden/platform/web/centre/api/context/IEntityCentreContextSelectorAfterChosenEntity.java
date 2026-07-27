package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

/// Continuation interface returned by [IEntityCentreContextWithChosenEntitySelector#withChosenEntity()].
///
/// Combines the four entry-level continuation steps from [IEntityCentreContextSelector] with the full extending surface from [IEntityCentreContextSelector6] —
/// the caller may continue the chain with another `with*` step, attach a computation, extend with insertion-point or parent-centre contexts, or finish immediately via `build()` for a chosen-entity-only configuration.
///
/// Does not expose `withChosenEntity()` — the opt-in is idempotent at the type level.
///
public interface IEntityCentreContextSelectorAfterChosenEntity<T extends AbstractEntity<?>> extends IEntityCentreContextSelector<T>, IEntityCentreContextSelector6<T> {
}
