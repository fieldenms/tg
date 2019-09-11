package ua.com.fielden.platform.web.centre.api.impl;

import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.DEFAULT_COLUMN_WIDTH;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement.MIN_COLUMN_WIDTH;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IDynamicPropConfig;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderDone;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWidth;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithDesc;
import ua.com.fielden.platform.web.centre.api.dynamicprops.IDynamicPropBuilderWithTitle;

public class DynamicProp<T extends AbstractEntity<?>> implements IDynamicPropBuilderWithTitle, IDynamicPropBuilderWithDesc {

    private final DynamicPropBuilder<T> dynamicPropBuilder;
    private final String keyPropValue;

    private String title;
    private Optional<String> desc = Optional.empty();
    private int width = DEFAULT_COLUMN_WIDTH;
    private int minWidth = MIN_COLUMN_WIDTH;
    private int growFactor = 1;

    public DynamicProp(final DynamicPropBuilder<T> dynamicPropBuilder, final String keyPropValue) {
        this.dynamicPropBuilder = dynamicPropBuilder;
        this.keyPropValue = keyPropValue;
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
    public IDynamicPropBuilderDone width(final int width) {
        this.width = width;
        this.growFactor = 0;
        return this;
    }

    @Override
    public IDynamicPropBuilderDone minWidth(final int minWidth) {
        this.minWidth = minWidth > this.width ? this.width: minWidth;
        return this;
    }

    @Override
    public IDynamicPropConfig done() {
        return dynamicPropBuilder;
    }

    public String getKeyPropValue() {
        return keyPropValue;
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
    public IDynamicPropBuilderWithTitle addProp(final String keyPropValue) {
        return dynamicPropBuilder.addProp(keyPropValue);
    }

}
