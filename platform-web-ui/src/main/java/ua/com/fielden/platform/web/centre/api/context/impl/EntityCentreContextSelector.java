package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.*;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EntityCentreContextSelector<T extends AbstractEntity<?>> implements IEntityCentreContextWithChosenEntitySelector<T> {

    private final boolean withChosenEntity;

    public static <T extends AbstractEntity<?>> IEntityCentreContextWithChosenEntitySelector<T> context() {
        return new EntityCentreContextSelector<>();
    }

    private EntityCentreContextSelector() {
        this.withChosenEntity = false;
    }

    private EntityCentreContextSelector(final boolean withChosenEntity) {
        this.withChosenEntity = withChosenEntity;
    }

    @Override
    public IEntityCentreContextSelector3<T> withSelectionCrit() {
        return new EntityCentreContextSelector3<>(withChosenEntity, true, false);
    }

    @Override
    public IEntityCentreContextSelector5<T> withMasterEntity() {
        return new EntityCentreContextSelector5<>(withChosenEntity, false, true);
    }

    @Override
    public IEntityCentreContextSelector0<T> withCurrentEntity() {
        return new EntityCentreContextSelector0<>(withChosenEntity, true, false);
    }

    @Override
    public IEntityCentreContextSelector0<T> withSelectedEntities() {
        return new EntityCentreContextSelector0<>(withChosenEntity, false, true);
    }

    @Override
    public IEntityCentreContextSelector<T> withChosenEntity() {
        return new EntityCentreContextSelector<>(true);
    }
}
