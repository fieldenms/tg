package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import ua.com.fielden.platform.entity.AbstractEntity;
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

public class ScatterPlotMasterBuilder<T extends AbstractEntity<?>> implements IScatterPlotMasterBuilder<T>, IScatterPlotCategoryProperty<T>, IScatterPlotValueProperty<T>, IScatterPlotTitle<T>, IScatterPlotStyleProperty<T> {

    private Class<T> entityType;
    private boolean saveOnActivation = false;
    private String categoryPropertyName;
    private String valuePropertyName;
    private String stylePropertyName;
    private String title;
    private String xAxisTitle;
    private String yAxisTitle;
    private EntityActionConfig action;
    private final List<String> tooltipPropertyNames = new ArrayList<>();
    private final List<Pair<Map<String, String>, String>> legendItems = new ArrayList<>();


    @Override
    public IScatterPlotCategoryProperty<T> forEntity(final Class<T> entityType) {
        this.entityType = entityType;
        return this;
    }

    @Override
    public IScatterPlotCategoryProperty<T> forEntityWithSaveOnActivation(final Class<T> entityType) {
        this.entityType = entityType;
        this.saveOnActivation = true;
        return this;
    }

    @Override
    public IScatterPlotValueProperty<T> setCategoryPropertyName(final String propertyName) {
        this.categoryPropertyName = propertyName;
        return this;
    }

    @Override
    public IScatterPlotStyleProperty<T> setValuePropertyName(final String propertyName) {
        this.valuePropertyName = valuePropertyName;
        return this;
    }

    @Override
    public IScatterPlotDone<T> withAction(final EntityActionConfig action) {
        this.action = action;
        return this;
    }

    @Override
    public IMaster<T> done() {
        return null;
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
    public IScatterPlotTooltip<T> addPropertyToTooltip(final String propertyName) {
        this.tooltipPropertyNames.add(propertyName);
        return this;
    }

    @Override
    public IScatterPlotYAxisTitle<T> withXAxisTitle(final String title) {
        this.xAxisTitle = title;
        return this;
    }

    @Override
    public IScatterPlotTooltip<T> withYAxisTitle(final String title) {
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
    public IScatterPlotTitle<T> setStylePropertyName(final String propertyName) {
        this.stylePropertyName = propertyName;
        return this;
    }

    public String getStylePropertyName() {
        return stylePropertyName;
    }

    public List<String> getTooltipProperties() {
        return Collections.unmodifiableList(tooltipPropertyNames);
    }
}