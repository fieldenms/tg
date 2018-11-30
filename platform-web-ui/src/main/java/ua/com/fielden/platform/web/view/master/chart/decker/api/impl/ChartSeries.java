package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesAction;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerXAxisTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerYAxisTitle;

public class ChartSeries<T extends AbstractEntity<?>> implements IChartDeckerWithSeriesTitle<T>, IChartDeckerWithSeriesAction<T>{

    private final ChartDeck<T> chartDeck;
    private final String propertyName;
    private String title;
    private Colour colour;
    private EntityActionConfig action;


    public ChartSeries(final ChartDeck<T> chartDeck, final String propertyName) {
        this.propertyName = propertyName;
        this.chartDeck = chartDeck;
    }

    @Override
    public IChartDeckerWithSeriesAction<T> colour(final Colour colour) {
        this.colour = colour;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesColour<T> title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesAlso<T> action(final EntityActionConfig action) {
        this.action = action;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesTitle<T> withSeries(final String propertyName) {
        return chartDeck.withSeries(propertyName);
    }

    @Override
    public IChartDeckerXAxisTitle<T> withTitle(final String title) {
        return chartDeck.withTitle(title);
    }

    @Override
    public IChartDeckerYAxisTitle<T> withXAxisTitle(final String title) {
        return chartDeck.withXAxisTitle(title);
    }

    @Override
    public IChartDeckerAlso<T> withYAxisTitle(final String title) {
        return chartDeck.withYAxisTitle(title);
    }

    @Override
    public IChartDeckerAddDeck<T> also() {
        return chartDeck.also();
    }

    @Override
    public IMaster<T> done() {
        return chartDeck.done();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Class<?> getPropertyType() {
        return PropertyTypeDeterminator.determinePropertyType(chartDeck.getEntityType(), propertyName);
    }
}
