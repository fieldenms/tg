package ua.com.fielden.platform.web.layout.grid;

/// A subheader element — an inserted, optionally collapsible section title that spans all columns.
/// Because a subheader always spans the full row, its span is not configurable; only styling and self-alignment may be set, and it carries no editor binding.
///
public interface ISubheader extends IGridElement {

    /// Emulates an arbitrary CSS declaration on the subheader.
    /// May be called more than once.
    ///
    ISubheader style(String property, String value);

    /// Sets the subheader's alignment along the inline (column) axis — maps to `justify-self`.
    ///
    ISubheader justify(String value);

    /// Sets the subheader's alignment along the block (row) axis — maps to `align-self`.
    ///
    ISubheader align(String value);
}