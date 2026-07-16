package ua.com.fielden.platform.web.layout.grid;

/// Container-level configuration of a grid layout — gaps, content distribution, item alignment defaults and arbitrary styles.
/// An instance is created via the static `content()` factory and passed to [IContentStep#content(IGridContent)].
/// The methods may be called in any order and any number of times.
///
public interface IGridContent {

    /// Distributes the grid tracks along the inline (column) axis — maps to `justify-content`.
    ///
    IGridContent justifyContent(String value);

    /// Distributes the grid tracks along the block (row) axis — maps to `align-content`.
    ///
    IGridContent alignContent(String value);

    /// Sets the default alignment of all cells along the inline (column) axis — maps to `justify-items`.
    ///
    IGridContent justifyItems(String value);

    /// Sets the default alignment of all cells along the block (row) axis — maps to `align-items`.
    ///
    IGridContent alignItems(String value);

    /// Sets the gaps between cells — `rowGap` first, then `columnGap`, matching the CSS `gap` shorthand order.
    ///
    IGridContent withGaps(String rowGap, String columnGap);

    /// Sets an arbitrary CSS declaration on the grid container (e.g. `padding`).
    /// May be called more than once.
    ///
    IGridContent style(String property, String value);

    /// Reserves a left indentation gutter for a layout that uses subheaders.
    /// When the layout contains at least one subheader, an implicit leading column of this width is added: only the subheaders span it (so their titles stay flush with the left edge), while every other element — including content before the first subheader — is indented past it, and the declared columns keep equal widths.
    /// The developer's columns and cell coordinates are unaffected — the gutter is entirely implicit.
    ///
    IGridContent withSubheaderIndentation(String size);

    /// Sets an arbitrary CSS declaration applied to every subheader of the layout (e.g. `padding-left`, `font-weight`, `color`).
    /// Unlike [#style(String, String)], which styles the grid container, this is the container-level default for what `subheader(row, title).style(property, value)` sets on a single subheader.
    /// A declaration set here is overridden, for the same property, by that property set on an individual subheader via its own `style(property, value)`.
    /// It participates in the same cascade as a per-subheader style — so, like the per-subheader value, it wins over column and row track styles.
    /// May be called more than once; declarations accumulate in the order given.
    ///
    IGridContent withSubheaderStyle(String property, String value);
}
