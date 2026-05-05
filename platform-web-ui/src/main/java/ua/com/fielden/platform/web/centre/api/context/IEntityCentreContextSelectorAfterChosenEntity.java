package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

/// Continuation interface returned by [IEntityCentreContextWithChosenEntitySelector#withChosenEntity()].
///
/// Combines the four entry-level continuation steps from [IEntityCentreContextSelector] with the terminal [IEntityCentreContextSelectorDone#build()],
/// which lets the caller either continue the chain with another `with*` step or finish immediately for a chosen-entity-only configuration.
///
/// Does not expose `withChosenEntity()` — the opt-in is idempotent at the type level.
///
public interface IEntityCentreContextSelectorAfterChosenEntity<T extends AbstractEntity<?>> extends IEntityCentreContextSelector<T>, IEntityCentreContextSelectorDone<T> {
}
