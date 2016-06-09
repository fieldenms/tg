package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector2;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector4;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorDone;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
class EntityCentreContextSelector1_2_4_done<T extends AbstractEntity<?>> implements IEntityCentreContextSelector1<T>, IEntityCentreContextSelector2<T>, IEntityCentreContextSelector4<T>, IEntityCentreContextSelectorDone<T> {

    private final boolean withCurrentEntity;
    private final boolean withAllSelectedEntities;
    private final boolean withSelectionCrit;
    private final boolean withMasterEntity;

    public EntityCentreContextSelector1_2_4_done(
            final boolean withCurrentEntity, final boolean withAllSelectedEntities,
            final boolean withSelectionCrit, final boolean withMasterEntity) {
        this.withCurrentEntity = withCurrentEntity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEntity,
                withAllSelectedEntities,
                withSelectionCrit,
                withMasterEntity
               );
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_done<T>(withCurrentEntity, withAllSelectedEntities, withSelectionCrit, true);
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_done<T>(withCurrentEntity, withAllSelectedEntities, true, withMasterEntity);
    }

    @Override
    public IEntityCentreContextSelector1<T> withCurrentEntity() {
        return new EntityCentreContextSelector1_2_4_done<T>(true, withAllSelectedEntities, withSelectionCrit, withMasterEntity);
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectedEntities() {
        return new EntityCentreContextSelector1_2_4_done<T>(withCurrentEntity, true, withSelectionCrit, withMasterEntity);
    }



}
