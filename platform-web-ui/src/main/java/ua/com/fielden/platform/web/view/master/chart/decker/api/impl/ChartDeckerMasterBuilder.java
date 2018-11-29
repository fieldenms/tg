package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerConfig;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerDesc;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerGroup;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMasterBuilder;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMasterDone;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithSeries;

public class ChartDeckerMasterBuilder<T extends AbstractEntity<?>> implements IChartDeckerConfig<T>, IChartDeckerMasterBuilder<T>, IChartDeckerMasterDone<T>, IChartDeckerGroup<T>, IChartDeckerDesc<T>{

    private final List<ChartDeck<T>> decks = new ArrayList<>();

    private Class<T> masterEntityType;
    private boolean saveOnActivation = false;
    private String groupKeyProperty;
    private String groupDescProperty = "desc";

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
    public IChartDeckerWithSeries<T> addDeckFor(final Class<? extends AbstractEntity<?>> entityType) {
        final ChartDeck<T> newDeck = new ChartDeck<>(entityType, this);
        decks.add(newDeck);
        return null;
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
    public String getGroupKeyPropoerty() {
        return groupKeyProperty;
    }

    @Override
    public List<ChartDeck<T>> getDecs() {
        return decks;
    }

    @Override
    public String getGroupDescProperty() {
        return groupDescProperty;
    }

    @Override
    public IChartDeckerAddDeck<T> groupDescProp(final String descriptionProperty) {
        this.groupDescProperty = descriptionProperty;
        return this;
    }

    @Override
    public IChartDeckerDesc<T> groupKeyProp(final String property) {
        this.groupKeyProperty = property;
        return this;
    }
}