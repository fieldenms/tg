package ua.com.fielden.platform.web.centre.api.context.impl;

import static java.util.Optional.empty;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.*;

/// Default implementation for the entity centre context selector API.
///
public class EntityCentreContextSelector<T extends AbstractEntity<?>> implements IEntityCentreContextWithChosenEntitySelector<T>, IEntityCentreContextSelectorAfterChosenEntity<T> {

    private final boolean withChosenEntity;

    public static <T extends AbstractEntity<?>> IEntityCentreContextWithChosenEntitySelector<T> context() {
        return new EntityCentreContextSelector<>(false);
    }

    private EntityCentreContextSelector(final boolean withChosenEntity) {
        this.withChosenEntity = withChosenEntity;
    }

    @Override
    public IEntityCentreContextSelector3<T> withSelectionCrit() {
        return new EntityCentreContextSelector3<>(true, false, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelector5<T> withMasterEntity() {
        return new EntityCentreContextSelector5<>(false, true, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelector0<T> withCurrentEntity() {
        return new EntityCentreContextSelector0<>(true, false, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelector0<T> withSelectedEntities() {
        return new EntityCentreContextSelector0<>(false, true, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelectorAfterChosenEntity<T> withChosenEntity() {
        return new EntityCentreContextSelector<>(true);
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(false, false, false, false, withChosenEntity, null, empty(), empty());
    }

}
