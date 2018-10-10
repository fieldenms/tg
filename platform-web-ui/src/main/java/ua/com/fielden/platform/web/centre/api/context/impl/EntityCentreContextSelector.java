package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector0;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector3;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector5;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EntityCentreContextSelector<T extends AbstractEntity<?>> implements IEntityCentreContextSelector<T> {


    public static <T extends AbstractEntity<?>> IEntityCentreContextSelector<T> context() {
        return new EntityCentreContextSelector<T>();
    }

    private EntityCentreContextSelector() {}


    @Override
    public IEntityCentreContextSelector3<T> withSelectionCrit() {
        return new EntityCentreContextSelector3<T>(true, false);
    }

    @Override
    public IEntityCentreContextSelector5<T> withMasterEntity() {
        return new EntityCentreContextSelector5<T>(false, true);
    }

    @Override
    public IEntityCentreContextSelector0<T> withCurrentEntity() {
        return new EntityCentreContextSelector0<T>(true, false);
    }

    @Override
    public IEntityCentreContextSelector0<T> withSelectedEntities() {
        return new EntityCentreContextSelector0<T>(false, true);
    }

}
