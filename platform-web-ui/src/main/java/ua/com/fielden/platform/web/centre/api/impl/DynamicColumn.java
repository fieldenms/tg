package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.*;
import ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.DEFAULT_COLUMN_WIDTH;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.resizeFloor;

/// Holds the per-column configuration produced by [DynamicColumnBuilder#addColumn(String)].
///
/// Acts as the receiver for the fluent calls that follow `addColumn`: `title`, `desc`, `width`/`minWidth`, and `withWordWrap`.
/// The `addColumn` method on this class delegates back to the parent builder to start a new column, and `done` finalises configuration.
///
/// [DynamicColumnBuilder#build()] reads the accumulated settings to produce the per-column attribute map keyed by the `DYN_COL_*` constants below.
/// Those keys are consumed by the EGI rendering layer — see `PropertyColumnElement` and the `tg-property-column` Polymer component.
///
/// **Width / minWidth model — shared with static columns (see [ResultSetBuilder]).**
///
/// A column has exactly one stored size — the *natural width* in the `width` field — together with a flexibility flag.
/// The flag here is `growFactor`; `isFlexible` for static columns.
/// Both [#width(int)] and [#minWidth(int)] write to the same `width` field; they only differ in the flexibility they produce:
/// - [#width(int)] → rigid (`growFactor = 0`); the column never grows beyond its natural width.
/// - [#minWidth(int)] → flexible (`growFactor` left at the default `1`); the column may grow to absorb leftover horizontal space.
///
/// At render time the CSS `min-width` of the cell is set from the natural width (see `tg-entity-grid-inspector._calcColumnHeaderStyle`).
/// So a column cannot be squeezed below its natural width by flex layout.
/// The separate "column min-width" attribute — [#DYN_COL_MIN_WIDTH] from [#getResizeFloor()] — is **only** the *drag-resize floor*.
/// It is capped at [PropertyColumnElement#MIN_COLUMN_WIDTH] (16px).
/// It is consumed solely by the JS resize handler (`_trackColumnSize`) to bound the new width when the user drags a column edge.
///
public class DynamicColumn<T extends AbstractEntity<?>> implements IDynamicColumnBuilderWithTitle, IDynamicColumnBuilderWithDesc, IDynamicColumnBuilderWordWrap {
    public static final String
            DYN_COL_GROUP_PROP_VALUE = "keyPropValue",
            DYN_COL_TYPE = "type",
            DYN_COL_GROUP_PROP = "keyProp",
            DYN_COL_DISPLAY_PROP = "valueProp",
            DYN_COL_TOOLTIP_PROP = "tooltipProp",
            DYN_COL_TITLE = "title",
            DYN_COL_DESC = "desc",
            DYN_COL_WIDTH = "width",
            DYN_COL_MIN_WIDTH = "minWidth",
            DYN_COL_GROW_FACTOR = "growFactor",
            DYN_COL_WORD_WRAP = "wordWrap";

    private final DynamicColumnBuilder<T> dynamicColumnBuilder;
    private final String groupPropValue;

    private String title;
    private Optional<String> desc = Optional.empty();
    /// The column's natural width. Used both as the CSS `min-width` (preventing flex shrink) and as the starting `width` of the cell.
    /// Written by [#width(int)] (rigid) and by [#minWidth(int)] (flexible) — both setters target the same field.
    ///
    private int width = DEFAULT_COLUMN_WIDTH;
    /// `0` → rigid (the column never grows beyond its natural width); `> 0` → flexible (CSS `flex-grow` applies).
    /// Defaults to `1`, matching `.minWidth(...)` semantics for columns that don't call `.width(...)` explicitly.
    ///
    private int growFactor = 1;
    private boolean wordWrap;

    public DynamicColumn(final DynamicColumnBuilder<T> dynamicColumnBuilder, final String groupPropValue) {
        this.dynamicColumnBuilder = dynamicColumnBuilder;
        this.groupPropValue = groupPropValue;
    }

    @Override
    public IDynamicColumnBuilderWithDesc title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public IDynamicColumnBuilderWidth desc(final String desc) {
        this.desc = ofNullable(desc);
        return this;
    }

    /// Sets the natural width and makes the column **rigid** (`growFactor = 0`).
    /// The column will render at `width` pixels and will not grow when the EGI is wider than the sum of column widths.
    /// During drag-resize the user can still shrink the column down to the drag-floor (see [#getResizeFloor()]).
    ///
    @Override
    public IDynamicColumnBuilderWordWrap width(final int width) {
        this.width = width;
        this.growFactor = 0;
        return this;
    }

    /// Sets the natural width and leaves the column **flexible** (`growFactor` stays at its default `1`).
    /// The column will render at `minWidth` pixels and may grow to absorb leftover horizontal space.
    /// Once the user manually resizes such a column, its `growFactor` is set to `0`, persisting the column as rigid from that point on.
    /// `minWidth` here is the user-supplied *natural width* for the flexible case — it is **not** the drag-resize floor.
    /// That is always [PropertyColumnElement#MIN_COLUMN_WIDTH] for any reasonable width.
    ///
    @Override
    public IDynamicColumnBuilderWordWrap minWidth(final int minWidth) {
        this.width = minWidth;
        return this;
    }

    @Override
    public IDynamicColumnBuilderAddPropWithDone withWordWrap() {
        this.wordWrap = true;
        return this;
    }

    @Override
    public IDynamicColumnConfig done() {
        return dynamicColumnBuilder;
    }

    public String getGroupPropValue() {
        return groupPropValue;
    }

    public Optional<String> getDesc() {
        return desc;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    /// The *drag-resize floor* — the lowest width a user can resize this column to.
    /// **Not** the user-supplied `minWidth` from the DSL; that goes into the natural width (`width`).
    /// Matches the static-column formula (see [PropertyColumnElement#resizeFloor(int)]).
    /// I.e. equals to [PropertyColumnElement#MIN_COLUMN_WIDTH] for any reasonable width.
    ///
    public int getResizeFloor() {
        return resizeFloor(width);
    }

    public int getGrowFactor() {
        return growFactor;
    }

    public boolean isWordWrap() {
        return wordWrap;
    }

    @Override
    public IDynamicColumnBuilderWithTitle addColumn(final String groupPropValue) {
        return dynamicColumnBuilder.addColumn(groupPropValue);
    }

}
