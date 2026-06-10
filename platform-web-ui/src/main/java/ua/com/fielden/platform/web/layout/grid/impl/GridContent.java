package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.com.fielden.platform.web.layout.grid.IGridContent;

/// Implementation of the container-level configuration of a CSS Grid layout.
///
/// CSS Grid offers limited container-level configuration: it can distribute the tracks (`justify-content` / `align-content`) and set a default alignment for all cells (`justify-items` / `align-items`).
/// Those item defaults cascade — they are overridden per track in the column and row configurations, and per cell in the element configuration.
/// Gaps and arbitrary styles (e.g. `padding`) are configured here as well.
///
public class GridContent implements IGridContent {

    private final Map<String, String> styles = new LinkedHashMap<>();

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

    /// Renders the configured CSS declarations as a list of `property: value` strings, in insertion order.
    ///
    List<String> declarations() {
        return styles.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());
    }
}
