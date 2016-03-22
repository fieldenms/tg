package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector3;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector4;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
class EntityCentreContextSelector3<T extends AbstractEntity<?>> implements IEntityCentreContextSelector3<T> {

    private final boolean withSelectionCrit;
    private final boolean withMasterEntity;


    public EntityCentreContextSelector3(final boolean withSelectionCrit, final boolean withMasterEntity) {
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                false,
                false,
                withSelectionCrit,
                withMasterEntity
               );
    }

    @Override
    public IEntityCentreContextSelector1<T> withCurrentEntity() {
        return new EntityCentreContextSelector1_2_4_done<T>(true, false, withSelectionCrit, withMasterEntity);
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectedEntities() {
        return new EntityCentreContextSelector1_2_4_done<T>(false, true, withSelectionCrit, withMasterEntity);
    }

    @Override
    public IEntityCentreContextSelector4<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_done<T>(false, false, withSelectionCrit, true);
    }



}
