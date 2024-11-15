package ua.com.fielden.platform.entity.query.model;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;

@Singleton
final class FillModelApplierImpl implements IFillModelApplier {

    @Override
    public <E extends AbstractEntity<?>> E apply(final IFillModel model, final E entity) {
        model.forEach(entity::set);
        return entity;
    }

}
