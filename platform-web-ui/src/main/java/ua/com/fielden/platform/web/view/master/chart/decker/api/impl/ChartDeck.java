package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerBarColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithAction;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerXAxisTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerYAxisTitle;

public class ChartDeck<T extends AbstractEntity<?>> implements IChartDeckerWithTitle<T> {

    private final String aggregationProperty;
    private final ChartDeckerMasterBuilder<T> deckerBuilder;

    private String title = "";
    private String xAxisTitle = "";
    private String yAxisTitle = "";
    private Colour barColour = new Colour("0288D1");
    private EntityActionConfig actionConfig;

    public ChartDeck(final String aggregationProperty, final ChartDeckerMasterBuilder<T> chartDeckerMasterBuilder) {
        this.aggregationProperty = aggregationProperty;
        this.deckerBuilder = chartDeckerMasterBuilder;
    }

    @Override
    public IChartDeckerYAxisTitle<T> withXAxisTitle(final String title) {
        this.xAxisTitle = title;
        return this;
    }

    @Override
    public IChartDeckerBarColour<T> withYAxisTitle(final String title) {
        this.yAxisTitle = title;
        return this;
    }

    @Override
    public IChartDeckerWithAction<T> withBarColour(final Colour barColour) {
        this.barColour = barColour;
        return this;
    }

    @Override
    public IChartDeckerAlso<T> withAction(final EntityActionConfig action) {
        this.actionConfig = action;
        return this;
    }

    @Override
    public IChartDeckerAddDeck<T> also() {
        return deckerBuilder;
    }

    @Override
    public IMaster<T> done() {
        return deckerBuilder.done();
    }

    @Override
    public IChartDeckerXAxisTitle<T> withTitle(final String title) {
        this.title = title;
        return this;
    }
}
