package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class EntityProducerWithNewEditActions<T extends AbstractEntity<?>, C extends AbstractEntity<?>> extends DefaultEntityProducerWithContext<T, C> {

    public EntityProducerWithNewEditActions(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        if (isEntityEditing()) {
            return companion().findById(Long.valueOf(((EntityEditAction) getMasterEntity()).getEntityId()), companion().getFetchProvider().fetchModel());
        }
        return provideDefaultValuesForNewEntity(entity);
    }

    protected T provideDefaultValuesForNewEntity(final T entity) {
        return entity;
    };

    private boolean isEntityEditing() {
        return getMasterEntity() != null && EntityEditAction.class.isAssignableFrom(getMasterEntity().getClass());
    }
}
