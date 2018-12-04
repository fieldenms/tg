package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerLineColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesAction;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeriesTitle;

public class ChartSeries<T extends AbstractEntity<?>> implements IChartDeckerWithSeriesColour<T>, IChartDeckerWithSeriesTitle<T>{

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
    public IChartDeckerWithSeriesTitle<T> colour(final Colour colour) {
        this.colour = colour;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesAction<T> title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesAlso<T> action(final EntityActionConfig action) {
        this.action = action;
        return this;
    }

    @Override
    public IChartDeckerWithSeriesColour<T> withSeries(final String propertyName) {
        return chartDeck.withSeries(propertyName);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Class<?> getPropertyType() {
        return PropertyTypeDeterminator.determinePropertyType(chartDeck.getEntityType(), propertyName);
    }

    public Colour getColour() {
        return colour;
    }

    public EntityActionConfig getAction() {
        return action;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public IChartDeckerLineColour<T> withLine(final String propertyName) {
        return chartDeck.withLine(propertyName);
    }

    @Override
    public IChartDeckerAddDeck<T> also() {
        return chartDeck.deckerBuilder;
    }

    @Override
    public IMaster<T> done() {
        return chartDeck.deckerBuilder.done();
    }
}
