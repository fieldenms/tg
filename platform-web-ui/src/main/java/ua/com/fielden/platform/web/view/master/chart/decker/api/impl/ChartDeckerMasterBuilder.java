package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerGroup;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMasterBuilder;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerMasterDone;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithTitle;

public class ChartDeckerMasterBuilder<T extends AbstractEntity<?>> implements IChartDeckerMasterBuilder<T>, IChartDeckerMasterDone<T>, IChartDeckerGroup<T>, IChartDeckerAddDeck<T>{

    private Class<T> masterEntityType;
    private boolean saveOnActivation = false;

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
        return new ChartDeckerMaster<T>(masterEntityType, saveOnActivation);
    }

    @Override
    public IChartDeckerAddDeck<T> groupBy(final String propeorty) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IChartDeckerWithTitle<T> addDeckForProperty(final String aggregationProperty) {
        // TODO Auto-generated method stub
        return null;
    }
}