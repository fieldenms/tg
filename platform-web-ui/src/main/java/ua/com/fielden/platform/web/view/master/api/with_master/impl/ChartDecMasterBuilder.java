package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IChartDecMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.IChartDecMasterDone;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.ChartDecMaster;

public class ChartDecMasterBuilder<T extends AbstractEntity<?>> implements IChartDecMasterBuilder<T>, IChartDecMasterDone<T>{

    private Class<T> masterEntityType;
    private boolean saveOnActivation = false;

    @Override
    public IChartDecMasterDone<T> forEntity(final Class<T> entityType) {
        this.masterEntityType = entityType;
        return this;
    }

    @Override
    public IChartDecMasterDone<T> forEntityWithSaveOnActivation(final Class<T> entityType) {
        this.masterEntityType = entityType;
        this.saveOnActivation = true;
        return this;
    }

    @Override
    public IMaster<T> done() {
        return new ChartDecMaster<T>(masterEntityType, saveOnActivation);
    }
}