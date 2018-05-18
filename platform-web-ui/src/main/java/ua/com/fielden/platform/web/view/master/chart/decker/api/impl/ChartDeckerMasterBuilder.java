package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerConfig;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerGroup;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMasterBuilder;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMasterDone;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithTitle;

public class ChartDeckerMasterBuilder<T extends AbstractEntity<?>> implements IChartDeckerConfig<T>, IChartDeckerMasterBuilder<T>, IChartDeckerMasterDone<T>, IChartDeckerGroup<T>, IChartDeckerAddDeck<T>{

    private final List<ChartDeck<T>> decks = new ArrayList<>();

    private Class<T> masterEntityType;
    private boolean saveOnActivation = false;
    private String groupByProperty;

    @Override
    public IChartDeckerGroup<T> forEntity(final Class<T> entityType) {
        this.masterEntityType = entityType;
        return this;
    }

    @Override
    public IChartDeckerGroup<T> forEntityWithSaveOnActivation(final Class<T> entityType) {
        this.masterEntityType = entityType;
        this.saveOnActivation = true;
        return this;
    }

    @Override
    public IMaster<T> done() {
        return new ChartDeckerMaster<T>(this);
    }

    @Override
    public IChartDeckerAddDeck<T> groupBy(final String property) {
        this.groupByProperty = property;
        return this;
    }

    @Override
    public IChartDeckerWithTitle<T> addDeckForProperty(final String aggregationProperty) {
        final ChartDeck<T> newDeck = new ChartDeck<>(aggregationProperty, this);
        decks.add(newDeck);
        return newDeck;
    }

    @Override
    public Class<T> getEntityType() {
        return masterEntityType;
    }

    @Override
    public boolean shouldSaveOnActivation() {
        return saveOnActivation;
    }

    @Override
    public String getGroupPropoerty() {
        return groupByProperty;
    }

    @Override
    public List<ChartDeck<T>> getDecs() {
        return decks;
    }
}