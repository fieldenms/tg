package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector0;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector2;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
class EntityCentreContextSelector0<T extends AbstractEntity<?>> implements IEntityCentreContextSelector0<T> {

    private final boolean withCurrentEntity;
    private final boolean withAllSelectedEntities;


    public EntityCentreContextSelector0(final boolean withCurrentEntity, final boolean withAllSelectedEntities) {
        this.withCurrentEntity = withCurrentEntity;
        this.withAllSelectedEntities = withAllSelectedEntities;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEntity,
                withAllSelectedEntities,
                false,
                false
               );
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_done<T>(withCurrentEntity, withAllSelectedEntities, true, false);
    }

    @Override
    public IEntityCentreContextSelector2<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_done<T>(withCurrentEntity, withAllSelectedEntities, false, true);
    }



}
