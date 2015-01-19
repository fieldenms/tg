package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgPersistentEntityWithInteger}.
 *
 * @author TG Team
 *
 */
public class TgPersistentEntityWithIntegerProducer implements IEntityProducer<TgPersistentEntityWithInteger> {
    private final EntityFactory factory;

    @Inject
    public TgPersistentEntityWithIntegerProducer(final EntityFactory factory) {
        this.factory = factory;
    }

    @Override
    public TgPersistentEntityWithInteger newEntity() {
        final TgPersistentEntityWithInteger entity = factory.newEntity(TgPersistentEntityWithInteger.class);
        return entity;
    }
}