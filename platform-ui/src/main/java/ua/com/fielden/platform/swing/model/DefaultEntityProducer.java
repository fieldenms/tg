package ua.com.fielden.platform.swing.model;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Provides default {@link EntityFactory} based implementation for creation of new entity instances.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class DefaultEntityProducer<T extends AbstractEntity> implements IEntityProducer<T> {

    private final EntityFactory factory;
    private final Class<T> entityType;

    public DefaultEntityProducer(final EntityFactory factory, final Class<T> entityType) {
        this.factory = factory;
        this.entityType = entityType;
    }

    @Override
    public T newEntity() {
        return factory.newEntity(entityType, null);
    }
}
