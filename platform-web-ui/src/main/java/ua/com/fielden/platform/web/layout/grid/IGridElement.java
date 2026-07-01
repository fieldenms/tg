package ua.com.fielden.platform.web.layout.grid;

/// A single element placed into a grid layout via `elements(...)` — the common supertype of every kind of element the grid accepts.
/// Concrete kinds are produced by the factories: a configured editor cell ([IGridCell], obtained by configuring the [ICell] from `cell(row, col)`), a `skip(...)` placeholder, an inline `html(...)` snippet, or a `subheader(...)` ([ISubheader]).
/// It declares no configuration of its own; each kind exposes only the configuration that is meaningful for it — for example, a subheader, whose column span is fixed, offers styling and alignment but no spans.
///
public interface IGridElement {
}