package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.web.layout.grid.IGridContent;

/// Implementation of the container-level configuration of a CSS Grid layout.
///
/// CSS Grid offers limited container-level configuration: it can distribute the tracks (`justify-content` / `align-content`) and set a default alignment for all cells (`justify-items` / `align-items`).
/// Those item defaults cascade — they are overridden per track in the column and row configurations, and per cell in the element configuration.
/// Gaps and arbitrary styles (e.g. `padding`) are configured here as well.
///
public class GridContent implements IGridContent {

    private final Map<String, String> styles = new LinkedHashMap<>();
    private final Map<String, String> subheaderStyles = new LinkedHashMap<>();
    private String subheaderIndentation;

    GridContent() {
    }

    @Override
    public GridContent justifyContent(final String value) {
        styles.put("justify-content", value);
        return this;
    }

    @Override
    public GridContent alignContent(final String value) {
        styles.put("align-content", value);
        return this;
    }

    @Override
    public GridContent justifyItems(final String value) {
        styles.put("justify-items", value);
        return this;
    }

    @Override
    public GridContent alignItems(final String value) {
        styles.put("align-items", value);
        return this;
    }

    @Override
    public GridContent withGaps(final String rowGap, final String columnGap) {
        styles.put("row-gap", rowGap);
        styles.put("column-gap", columnGap);
        return this;
    }

    @Override
    public GridContent style(final String property, final String value) {
        styles.put(property, value);
        return this;
    }

    @Override
    public GridContent withSubheaderIndentation(final String size) {
        this.subheaderIndentation = size;
        return this;
    }

    @Override
    public GridContent withSubheaderStyle(final String property, final String value) {
        subheaderStyles.put(property, value);
        return this;
    }

    /// Indicates whether any container-level declaration has been set.
    ///
    boolean isEmpty() {
        return styles.isEmpty();
    }

    /// The left indentation reserved for content beneath subheaders (the implicit gutter width), or `null` when not configured.
    ///
    String subheaderIndentation() {
        return subheaderIndentation;
    }

    /// The CSS declarations applied by default to every subheader, in declaration order; empty when none configured.
    /// These are folded into each subheader cell's own styles as defaults at build time (see [GridLayoutBuilder]), where a per-subheader declaration for the same property wins.
    /// The returned map is the live backing map — callers must treat it as read-only.
    ///
    Map<String, String> subheaderStyles() {
        return subheaderStyles;
    }

    /// Renders the configured declarations as a JavaScript object literal of `property: value` pairs, e.g. `{"row-gap":"0px","padding":"20px"}`.
    ///
    String renderObject() {
        return GridStyles.object(styles);
    }
}