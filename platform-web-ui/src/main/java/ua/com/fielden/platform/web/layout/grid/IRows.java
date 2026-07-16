package ua.com.fielden.platform.web.layout.grid;

/// The step at which the first row track is added.
/// At least one row track must be declared via [#addRow(String)] or [#addRow()] once row declaration has begun.
///
public interface IRows {

    /// Adds a row track with the given CSS track size, e.g. `auto`, `min-content`, `minmax(48px, auto)` or `100px`.
    ///
    IRow addRow(String size);

    /// Adds a row track sized `auto` — tall enough to fit its content.
    ///
    IRow addRow();
}
