package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgPersistentEntityWithProperties}.
 *
 * @author TG Team
 *
 */
public class TgPersistentEntityWithPropertiesProducer implements IEntityProducer<TgPersistentEntityWithProperties> {
    private final EntityFactory factory;

    @Inject
    public TgPersistentEntityWithPropertiesProducer(final EntityFactory factory) {
        this.factory = factory;
    }

    @Override
    public TgPersistentEntityWithProperties newEntity() {
        final TgPersistentEntityWithProperties entity = factory.newEntity(TgPersistentEntityWithProperties.class);

        // TODO provide some default properties and test whether "defaulting" works appropriately
        // TODO provide some default properties and test whether "defaulting" works appropriately
        // TODO provide some default properties and test whether "defaulting" works appropriately
        // TODO provide some default properties and test whether "defaulting" works appropriately
        // TODO provide some default properties and test whether "defaulting" works appropriately

        return entity;
    }
}