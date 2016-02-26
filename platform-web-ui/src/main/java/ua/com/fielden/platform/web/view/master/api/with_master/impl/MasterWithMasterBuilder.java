package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.IMasterWithMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.helpers.IComplete;
import ua.com.fielden.platform.web.view.master.api.with_master.IMasterWithMaster0;

public class MasterWithMasterBuilder<T extends AbstractFunctionalEntityWithCentreContext<?>> implements IMasterWithMasterBuilder<T>, IMasterWithMaster0<T>, IComplete<T> {

    private Class<T> type;
    private EntityMaster<?> entityMaster;

    @Override
    public IMasterWithMaster0<T> forEntityWithSaveOnActivate(final Class<T> type) {
        this.type = type;
        return this;
    }

    @Override
    public IComplete<T> withMaster(final EntityMaster<?> entityMaster) {
        this.entityMaster = entityMaster;
        return this;
    }

    @Override
    public IMaster<T> done() {
        return new MasterWithMaster<T>(type, entityMaster);
    }

}
