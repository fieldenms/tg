package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerLineAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerLineColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerLineTitle;

public class ChartLine<T extends AbstractEntity<?>> implements IChartDeckerLineColour<T>, IChartDeckerLineTitle<T> {

    private final ChartDeck<T> chartDeck;
    private final String property;
    private Colour colour;
    private String title;

    public ChartLine(final ChartDeck<T> chartDeck, final String property) {
        this.chartDeck = chartDeck;
        this.property = property;
    }

    @Override
    public IChartDeckerLineColour<T> withLine(final String propertyName) {
        return this.chartDeck.withLine(propertyName);
    }

    @Override
    public IChartDeckerAddDeck<T> also() {
        return chartDeck.deckerBuilder;
    }

    @Override
    public IMaster<T> done() {
        return chartDeck.deckerBuilder.done();
    }

    @Override
    public IChartDeckerLineAlso<T> title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public IChartDeckerLineTitle<T> colour(final Colour colour) {
        this.colour = colour;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Colour getColour() {
        return colour;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getPropertyType() {
        return PropertyTypeDeterminator.determinePropertyType(chartDeck.getEntityType(), this.property);
    }
}
