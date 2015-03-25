package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContexSelector;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContexSelector0;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContexSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContexSelector2;

public class EntityCentreContextSelector<T extends AbstractEntity<?>> implements IEntityCentreContexSelector<T> {

    private boolean withCurrentEtity = false;
    private boolean withAllSelectedEntities = false;
    private boolean withSelectionCrit = false;
    private boolean withMasterEntity = false;

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEtity,
                withAllSelectedEntities,
                withSelectionCrit,
                withMasterEntity
               );
    }

    @Override
    public IEntityCentreContexSelector1<T> withSelectionCrit() {
        this.withAllSelectedEntities = true;
        return this;
    }

    @Override
    public IEntityCentreContexSelector2<T> withMasterEntity() {
        this.withMasterEntity = true;
        return this;
    }

    @Override
    public IEntityCentreContexSelector0<T> withCurrentEntity() {
        this.withCurrentEtity = true;
        return this;
    }

    @Override
    public IEntityCentreContexSelector0<T> withSelectedEntities() {
        this.withSelectionCrit = true;
        return this;
    }

}
