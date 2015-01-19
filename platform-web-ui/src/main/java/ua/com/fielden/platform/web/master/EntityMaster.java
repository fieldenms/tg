package ua.com.fielden.platform.web.master;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.swing.model.DefaultEntityProducer;

import com.google.inject.Injector;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> {
    private final Class<T> entityType;
    private final fetch<T> fetchStrategy;
    private final Class<? extends IEntityProducer<T>> entityProducerType;

    /**
     * Creates master for the specified <code>entityType</code>, <code>fetchStrategy</code> and <code>entityProducerType</code>.
     *
     * @param entityType
     * @param fetchStrategy
     * @param entityProducerType
     *
     */
    public EntityMaster(final Class<T> entityType, final fetch<T> fetchStrategy, final Class<? extends IEntityProducer<T>> entityProducerType) {
        this.entityType = entityType;
        this.fetchStrategy = fetchStrategy;
        this.entityProducerType = entityProducerType;
    }

    /**
     * Creates master for the specified <code>entityType</code>, <code>fetchStrategy</code> and default entity producer.
     *
     * @param entityType
     * @param fetchStrategy
     *
     */
    public EntityMaster(final Class<T> entityType, final fetch<T> fetchStrategy) {
        this(entityType, fetchStrategy, null);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public fetch<T> getFetchStrategy() {
        return fetchStrategy;
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
