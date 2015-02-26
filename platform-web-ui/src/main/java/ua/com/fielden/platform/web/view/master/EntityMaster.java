package ua.com.fielden.platform.web.view.master;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.DefaultEntityProducer;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

import com.google.inject.Injector;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> implements IMaster<T> {
    private final Class<T> entityType;
    private final Class<? extends IEntityProducer<T>> entityProducerType;
    private final IRenderable masterComponent;
    private final Injector injector;

    /**
     * Creates master for the specified <code>entityType</code> and <code>entityProducerType</code>.
     *
     * @param entityType
     * @param entityProducerType
     * @param masterComponent
     *
     */
    public EntityMaster(final Class<T> entityType, final Class<? extends IEntityProducer<T>> entityProducerType, final IRenderable masterComponent, final Injector injector) {
        this.entityType = entityType;
        this.entityProducerType = entityProducerType;
        this.masterComponent = masterComponent;
        this.injector = injector;
    }

    /**
     * Creates master for the specified <code>entityType</code> and default entity producer.
     *
     * @param entityType
     * @param masterComponent
     *
     */
    public EntityMaster(final Class<T> entityType, final IRenderable masterComponent, final Injector injector) {
        this(entityType, null, masterComponent, injector);
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
    public IEntityProducer<T> createEntityProducer() {
        return entityProducerType == null ? new DefaultEntityProducer<T>(injector.getInstance(EntityFactory.class), this.entityType)
                : injector.getInstance(this.entityProducerType);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public IValueMatcher<AbstractEntity<?>> createValueMatcher(final String propertyName) {
        // TODO implement
        return null;
        // return entityProducerType == null ? new DefaultEntityProducer<T>(injector.getInstance(EntityFactory.class), this.entityType) : injector.getInstance(this.entityProducerType);
    }

    @Override
    public IRenderable build() {
        return masterComponent;
    }

}
