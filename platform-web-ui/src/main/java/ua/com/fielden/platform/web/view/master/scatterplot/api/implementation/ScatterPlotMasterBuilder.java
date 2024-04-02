package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.*;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartDeckerMaster;
import ua.com.fielden.platform.web.view.master.scatterplot.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScatterPlotMasterBuilder<T extends AbstractEntity<?>> implements IScatterPlotMasterBuilder<T>, IScatterPlotEntityType<T>, IScatterPlotCategoryProperty<T>, IScatterPlotValueProperty<T>, IScatterPlotTitle<T>, IScatterPlotStyleProperty<T> {

    private Class<T> entityType;
    private boolean saveOnActivation = false;
    private Class<? extends AbstractEntity<?>> chartEntityType;
    private String categoryPropertyName;
    private IScatterPlotRangeConfig categoryPropRangeSource;
    private String valuePropertyName;
    private IScatterPlotRangeConfig valuePropRangeSource;
    private String stylePropertyName;
    private String title;
    private String xAxisTitle;
    private String yAxisTitle;
    private int topMargin = 0;
    private int leftMargin = 0;
    private int bottomMargin = 0;
    private int rightMargin = 0;
    private EntityActionConfig action;
    private final List<IConvertableToPath> tooltipPropertyNames = new ArrayList<>();
    private final List<Pair<Map<String, String>, String>> legendItems = new ArrayList<>();


    @Override
    public IScatterPlotEntityType<T> forEntity(final Class<T> entityType) {
        this.entityType = entityType;
        return this;
    }

    @Override
    public IScatterPlotEntityType<T> forEntityWithSaveOnActivation(final Class<T> entityType) {
        this.entityType = entityType;
        this.saveOnActivation = true;
        return this;
    }

    @Override
    public IScatterPlotValueProperty<T> configCategoryProperty(final IConvertableToPath propertyName, IScatterPlotRangeConfig rangeConfig) {
        this.categoryPropertyName = propertyName.toPath();
        this.categoryPropRangeSource = rangeConfig;
        return this;
    }

    @Override
    public IScatterPlotStyleProperty<T> configValueProperty(final IConvertableToPath propertyName, IScatterPlotRangeConfig rangeConfig) {
        this.valuePropertyName = propertyName.toPath();
        this.valuePropRangeSource = rangeConfig;
        return this;
    }

    @Override
    public IScatterPlotDone<T> withAction(final EntityActionConfig action) {
        this.action = action;
        return this;
    }

    @Override
    public IMaster<T> done() {
        return new ScatterPlotMaster<>(this);
    }

    @Override
    public IScatterPlotLegend<T> addLegendItem(final Map<String, String> style, final String title) {
        this.legendItems.add(Pair.pair(style, title));
        return this;
    }

    @Override
    public IScatterPlotXAxisTitle<T> withTitle(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public IScatterPlotTooltip<T> addPropertyToTooltip(final IConvertableToPath propertyName) {
        this.tooltipPropertyNames.add(propertyName);
        return this;
    }

    @Override
    public IScatterPlotYAxisTitle<T> withXAxisTitle(final String title) {
        this.xAxisTitle = title;
        return this;
    }

    @Override
    public IScatterPlotTopMargin<T> withYAxisTitle(final String title) {
        this.yAxisTitle = title;
        return this;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public EntityActionConfig getAction() {
        return action;
    }

    public boolean shouldSaveOnActivation() {
        return this.saveOnActivation;
    }

    public List<Pair<Map<String, String>, String>> getLegend() {
        return Collections.unmodifiableList(this.legendItems);
    }

    public String getTitle() {
        return title;
    }

    public String getXAxisTitle() {
        return xAxisTitle;
    }

    public String getYAxisTitle() {
        return yAxisTitle;
    }

    public String getCategoryPropertyName() {
        return categoryPropertyName;
    }

    public String getValuePropertyName() {
        return valuePropertyName;
    }

    @Override
    public IScatterPlotTitle<T> setStylePropertyName(final IConvertableToPath propertyName) {
        return setStylePropertyName(propertyName.toPath());
    }

    @Override
    public IScatterPlotTitle<T> setStylePropertyName(final String propertyName) {
        this.stylePropertyName = propertyName;
        return this;
    }

    public String getStylePropertyName() {
        return stylePropertyName;
    }

    public List<IConvertableToPath> getTooltipProperties() {
        return Collections.unmodifiableList(tooltipPropertyNames);
    }

    @Override
    public IScatterPlotCategoryProperty<T> setChartEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        this.chartEntityType = entityType;
        return this;
    }

    public Class<? extends AbstractEntity<?>> getChartEntityType() {
        return chartEntityType;
    }

    public IScatterPlotRangeConfig getCategroyRangeConfig() {
        return categoryPropRangeSource;
    }

    public IScatterPlotRangeConfig getValueRangeConfig() {
        return valuePropRangeSource;
    }

    @Override
    public IScatterPlotRightMargin<T> bottomMargin(final int margin) {
        this.bottomMargin =  margin;
        return this;
    }

    @Override
    public IScatterPlotBottomMargin<T> leftMargin(final int margin) {
        this.leftMargin = margin;
        return this;
    }

    @Override
    public IScatterPlotTooltip<T> rightMargin(final int margin) {
        this.rightMargin = margin;
        return this;
    }

    @Override
    public IScatterPlotLeftMargin<T> topMargin(final int margin) {
        this.topMargin = margin;
        return this;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }
}