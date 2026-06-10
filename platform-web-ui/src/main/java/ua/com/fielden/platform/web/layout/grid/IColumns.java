package ua.com.fielden.platform.web.layout.grid;

/// The step at which the first column track is added.
/// At least one column track must be declared via [#addColumn(String)] before the layout can proceed.
///
public interface IColumns {

    /// Adds a column track with the given CSS track size, e.g. `1fr`, `minmax(20%, 1fr)`, `fit-content(30%)`, `auto` or `200px`.
    ///
    IColumn addColumn(String size);

    /// Adds a column track sized `1fr` — an equal share of the free space.
    ///
    IColumn addColumn();
}
