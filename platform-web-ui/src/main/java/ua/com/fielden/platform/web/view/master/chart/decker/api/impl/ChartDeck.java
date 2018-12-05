package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.chart.decker.api.BarMode;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerLineColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMode;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerShowLegend;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithLine;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeries;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerXAxisLabelOrientation;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerXAxisTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerYAxisTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.LabelOrientation;

public class ChartDeck<T extends AbstractEntity<?>> implements IChartDeckerMode<T>, IChartDeckerWithSeries<T>, IChartDeckerWithLine<T>{

    public final ChartDeckerMasterBuilder<T> deckerBuilder;

    private final List<ChartSeries<T>> series = new ArrayList<>();
    private final List<ChartLine<T>> lines = new ArrayList<>();
    private final Class<? extends AbstractEntity<?>> entityType;

    private BarMode mode = BarMode.GROUPED;
    private LabelOrientation xAxisLabelOrientation = LabelOrientation.HORIZONTAL;
    private boolean showLegend= false;
    private String title = "";
    private String xAxisTitle = "";
    private String yAxisTitle = "";

    public ChartDeck(final Class<? extends AbstractEntity<?>> entityType, final ChartDeckerMasterBuilder<T> chartDeckerMasterBuilder) {
        this.entityType = entityType;
        this.deckerBuilder = chartDeckerMasterBuilder;
    }

    @Override
    public IChartDeckerXAxisLabelOrientation<T> withXAxisTitle(final String title) {
        this.xAxisTitle = title;
        return this;
    }

    @Override
    public IChartDeckerXAxisTitle<T> withTitle(final String title) {
        this.title = title;
        return this;
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

    public String getGroupKeyProp() {
        return deckerBuilder.getGroupKeyPropoerty();
    }

    public String getGroupDescProperty() {
        return deckerBuilder.getGroupDescProperty();
    }

    public List<EntityActionConfig> getActions() {
        final List<EntityActionConfig> actions = new ArrayList<>();
        this.series.stream().forEach(s -> {
            if (s.getAction() != null) {
                actions.add(s.getAction());
            }
        });
        return actions;
    }

    @Override
    public IChartDeckerShowLegend<T> mode(final BarMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public IChartDeckerWithTitle<T> showLegend() {
        this.showLegend = true;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesColour<T> withSeries(final String propertyName) {
        final ChartSeries<T> series = new ChartSeries<>(this, propertyName);
        if (!this.series.stream().allMatch(s -> s.getPropertyType().equals(series.getPropertyType()))) {
            throw new ChartConfigurationError(format("The chart series should have the same type: %. But there was attempt to add series with different type: %",
                    this.series.get(0).getPropertyType().getSimpleName(),
                    series.getPropertyType().getSimpleName()));
        }
        this.series.add(series);
        return series;
    }

    public BarMode getMode() {
        return mode;
    }

    public Class<? extends AbstractEntity<?>> getEntityType() {
        return entityType;
    }

    public List<ChartSeries<T>> getSeries() {
        return Collections.unmodifiableList(series);
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    @Override
    public IChartDeckerLineColour<T> withLine(final String propertyName) {
        final ChartLine<T> line = new ChartLine<>(this, propertyName);
        this.lines.add(line);
        return line;
    }

    public List<ChartLine<T>> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public IChartDeckerWithSeries<T> withYAxisTitle(final String title) {
        this.yAxisTitle = title;
        return this;
    }

    @Override
    public IChartDeckerYAxisTitle<T> withXAxisLabelOrientation(final LabelOrientation orientation) {
        this.xAxisLabelOrientation = orientation;
        return this;
    }

    public LabelOrientation getxAxisLabelOrientation() {
        return xAxisLabelOrientation;
    }

    public Class<?> getPropertyType() {
        return PropertyTypeDeterminator.determinePropertyType(entityType, getGroupKeyProp());
    }
}
