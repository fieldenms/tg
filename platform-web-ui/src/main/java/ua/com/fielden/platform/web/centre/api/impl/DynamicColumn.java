package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.*;

import java.util.Optional;

import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.DEFAULT_COLUMN_WIDTH;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.MIN_COLUMN_WIDTH;

/// Holds the per-column configuration produced by [DynamicColumnBuilder#addColumn(String)].
///
/// Acts as the receiver for the fluent calls that follow `addColumn`: `title`, `desc`, `width`/`minWidth`, and `withWordWrap`.
/// The `addColumn` method on this class delegates back to the parent builder to start a new column, and `done` finalises configuration.
///
/// [DynamicColumnBuilder#build()] reads the accumulated settings to produce the per-column attribute map keyed by the `DYN_COL_*` constants below.
/// Those keys are consumed by the EGI rendering layer — see `PropertyColumnElement` and the `tg-property-column` Polymer component.
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
    private int width = DEFAULT_COLUMN_WIDTH;
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

    @Override
    public IDynamicColumnBuilderWordWrap width(final int width) {
        this.width = width;
        this.growFactor = 0;
        return this;
    }

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

    public int getMinWidth() {
        return min(width, MIN_COLUMN_WIDTH);
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
