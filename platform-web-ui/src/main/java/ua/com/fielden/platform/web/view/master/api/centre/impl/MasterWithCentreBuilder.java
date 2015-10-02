package ua.com.fielden.platform.web.view.master.api.centre.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.IMasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.api.centre.IMasterWithCentre0;
import ua.com.fielden.platform.web.view.master.api.helpers.IComplete;

public class MasterWithCentreBuilder<T extends AbstractEntity<?>> implements IMasterWithCentreBuilder<T>, IMasterWithCentre0<T>, IComplete<T> {

    private Class<T> type;
    private EntityCentre<?> entityCentre;
    private boolean saveOnActivate = false;

    @Override
    public IMasterWithCentre0<T> forEntity(final Class<T> type) {
        this.type = type;
        return this;
    }

    @Override
    public IMasterWithCentre0<T> forEntityWithSaveOnActivate(final Class<T> type) {
        this.type = type;
        this.saveOnActivate = true;
        return this;
    }

    @Override
    public IComplete<T> withCentre(final EntityCentre<?> entityCentre) {
        this.entityCentre = entityCentre;
        return this;
    }

    @Override
    public IMaster<T> done() {
        // TODO Auto-generated method stub
        return new MasterWithCentreConfig<T>(type, saveOnActivate, entityCentre);
    }

}
