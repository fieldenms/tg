package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderAddPropWithDone;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderWidth;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderWithDesc;
import ua.com.fielden.platform.web.centre.api.dynamic_columns.IDynamicColumnBuilderWithTitle;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.DEFAULT_COLUMN_WIDTH;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.MIN_COLUMN_WIDTH;

public class DynamicColumn<T extends AbstractEntity<?>> implements IDynamicColumnBuilderWithTitle, IDynamicColumnBuilderWithDesc {
    public static final String DYN_COL_GROUP_PROP_VALUE = "keyPropValue";
    public static final String DYN_COL_TYPE = "type";
    public static final String DYN_COL_GROUP_PROP = "keyProp";
    public static final String DYN_COL_DISPLAY_PROP = "valueProp";
    public static final String DYN_COL_TOOLTIP_PROP = "tooltipProp";
    public static final String DYN_COL_TITLE = "title";
    public static final String DYN_COL_DESC = "desc";
    public static final String DYN_COL_WIDTH = "width";
    public static final String DYN_COL_MIN_WIDTH = "minWidth";
    public static final String DYN_COL_GROW_FACTOR = "growFactor";
    public static final String DYN_COL_WORDWRAP="wordWrap";

    
    private final DynamicColumnBuilder<T> dynamicColumnBuilder;
    private final String groupPropValue;

    private String title;
    private Optional<String> desc = Optional.empty();
    private int width = DEFAULT_COLUMN_WIDTH;
    private int minWidth = MIN_COLUMN_WIDTH;
    private int growFactor = 1;

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
    public IDynamicColumnBuilderAddPropWithDone width(final int width) {
        this.width = width;
        this.growFactor = 0;
        return this;
    }

    @Override
    public IDynamicColumnBuilderAddPropWithDone minWidth(final int minWidth) {
        this.minWidth = minWidth > this.width ? this.width: minWidth;
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
        return minWidth;
    }

    public int getGrowFactor() {
        return growFactor;
    }

    @Override
    public IDynamicColumnBuilderWithTitle addColumn(final String groupPropValue) {
        return dynamicColumnBuilder.addColumn(groupPropValue);
    }

}
