package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
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
    private final ITgPersistentEntityWithProperties coTgPersistentEntityWithProperties;

    @Inject
    public TgPersistentEntityWithPropertiesProducer(final EntityFactory factory, final ITgPersistentEntityWithProperties coTgPersistentEntityWithProperties) {
        this.factory = factory;
        this.coTgPersistentEntityWithProperties = coTgPersistentEntityWithProperties;
    }

    @Override
    public TgPersistentEntityWithProperties newEntity() {
        final TgPersistentEntityWithProperties entity = factory.newEntity(TgPersistentEntityWithProperties.class);

        final TgPersistentEntityWithProperties defValue =
                //                coTgPersistentEntityWithProperties.getEntity(from(
                //                select(TgPersistentEntityWithProperties.class).where().prop("key").eq().val("DEFAULT_KEY").modelAsEntity(TgPersistentEntityWithProperties.class)
                //                ).with(fetchOnly(TgPersistentEntityWithProperties.class).with("key")).model());
                //                coTgPersistentEntityWithProperties.findById(12L, fetchOnly(TgPersistentEntityWithProperties.class).with("key"));
                coTgPersistentEntityWithProperties.findByKeyAndFetch(fetchOnly(TgPersistentEntityWithProperties.class).with("key"), "DEFAULT_KEY");

        System.out.println("defValue.getProperty(producerInitProp).isProxy() == " + defValue.getProperty("producerInitProp").isProxy());

        entity.setProducerInitProp(defValue);
        return entity;
    }
}