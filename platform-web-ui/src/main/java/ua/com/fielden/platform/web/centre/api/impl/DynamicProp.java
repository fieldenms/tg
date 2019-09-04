package ua.com.fielden.platform.web.centre.api.impl;

import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.DEFAULT_COLUMN_WIDTH;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.MIN_COLUMN_WIDTH;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IDynamicPropConfig;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IAlsoDynamicProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderAddProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderValueProp;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWidth;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithDesc;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithTitle;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithTooltipProp;

public class DynamicProp<T extends AbstractEntity<?>> implements IDynamicPropBuilderValueProp, IDynamicPropBuilderWithTooltipProp, IDynamicPropBuilderWithDesc {

    private final DynamicPropBuilder<T> dynamicPropBuilder;
    private final String keyProp;
    private final String type;

    private String valueProp;
    private Optional<String> tooltipProp = Optional.empty();
    private String title;
    private Optional<String> desc = Optional.empty();
    private int width = DEFAULT_COLUMN_WIDTH;
    private int minWidth = MIN_COLUMN_WIDTH;

    public DynamicProp(final DynamicPropBuilder<T> dynamicPropBuilder, final String keyProp, final String type) {
        this.dynamicPropBuilder = dynamicPropBuilder;
        this.keyProp = keyProp;
        this.type = type;
    }

    @Override
    public IDynamicPropBuilderWithTitle tooltipProp(final String tooltipProp) {
        this.tooltipProp = Optional.ofNullable(tooltipProp);
        return this;
    }

    @Override
    public IDynamicPropBuilderWithDesc title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public IDynamicPropBuilderWidth descripton(final String desc) {
        this.desc = Optional.ofNullable(desc);
        return this;
    }

    @Override
    public IAlsoDynamicProp width(final int width) {
        this.width = width;
        return this;
    }

    @Override
    public IAlsoDynamicProp minWidth(final int minWidth) {
        this.minWidth = minWidth > this.width ? this.width: minWidth;
        return this;
    }

    @Override
    public IDynamicPropBuilderAddProp also() {
        return this.dynamicPropBuilder;
    }

    @Override
    public IDynamicPropConfig done() {
        return dynamicPropBuilder;
    }

    @Override
    public IDynamicPropBuilderWithTooltipProp valueProp(final String valueProp) {
        this.valueProp = valueProp;
        return this;
    }

    public String getKeyProp() {
        return keyProp;
    }

    public String getType() {
        return type;
    }

    public String getValueProp() {
        return valueProp;
    }

    public Optional<String> getDesc() {
        return desc;
    }

    public Optional<String> getTooltipProp() {
        return tooltipProp;
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


}
