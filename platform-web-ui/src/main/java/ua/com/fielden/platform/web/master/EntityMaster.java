package ua.com.fielden.platform.web.master;

import ua.com.fielden.platform.dao.DefaultEntityProducer;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Injector;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> {
    private final Class<T> entityType;
    private final Class<? extends IEntityProducer<T>> entityProducerType;

    /**
     * Creates master for the specified <code>entityType</code> and <code>entityProducerType</code>.
     *
     * @param entityType
     * @param fetchStrategy
     * @param entityProducerType
     *
     */
    public EntityMaster(final Class<T> entityType, final Class<? extends IEntityProducer<T>> entityProducerType) {
        this.entityType = entityType;
        this.entityProducerType = entityProducerType;
    }

    /**
     * Creates master for the specified <code>entityType</code> and default entity producer.
     *
     * @param entityType
     * @param fetchStrategy
     *
     */
    public EntityMaster(final Class<T> entityType) {
        this(entityType, null);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Creates an entity producer instance.
     *
     * @param injector
     * @return
     */
    public IEntityProducer<T> createEntityProducer(final Injector injector) {
        return entityProducerType == null ? new DefaultEntityProducer<T>(injector.getInstance(EntityFactory.class), this.entityType) : injector.getInstance(this.entityProducerType);
    }
}
