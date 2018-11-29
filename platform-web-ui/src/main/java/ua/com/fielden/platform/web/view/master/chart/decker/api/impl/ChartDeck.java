package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerXAxisTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerYAxisTitle;

public class ChartDeck<T extends AbstractEntity<?>> implements IChartDeckerWithTitle<T> {

    private final List<ChartSeries<T>> series = new ArrayList<>();
    private final Class<? extends AbstractEntity<?>> entityType;
    private final ChartDeckerMasterBuilder<T> deckerBuilder;


    private String title = "";
    private String xAxisTitle = "";
    private String yAxisTitle = "";

    private EntityActionConfig actionConfig;

    public ChartDeck(final Class<? extends AbstractEntity<?>> entityType, final ChartDeckerMasterBuilder<T> chartDeckerMasterBuilder) {
        this.entityType = entityType;
        this.deckerBuilder = chartDeckerMasterBuilder;
    }

    @Override
    public IChartDeckerYAxisTitle<T> withXAxisTitle(final String title) {
        this.xAxisTitle = title;
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

    public EntityActionConfig getAction() {
        return actionConfig;
    }

    @Override
    public IChartDeckerAlso<T> withYAxisTitle(final String title) {
        // TODO Auto-generated method stub
        return null;
    }
}
