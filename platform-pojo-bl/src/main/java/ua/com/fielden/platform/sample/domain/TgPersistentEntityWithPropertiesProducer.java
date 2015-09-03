package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducer;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

/**
 * A producer for new instances of entity {@link TgPersistentEntityWithProperties}.
 *
 * @author TG Team
 *
 */
public class TgPersistentEntityWithPropertiesProducer extends DefaultEntityProducer<TgPersistentEntityWithProperties> implements IEntityProducer<TgPersistentEntityWithProperties> {
    private final ITgPersistentEntityWithProperties coTgPersistentEntityWithProperties;

    @Inject
    public TgPersistentEntityWithPropertiesProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgPersistentEntityWithProperties coTgPersistentEntityWithProperties) {
        super(factory, TgPersistentEntityWithProperties.class, companionFinder);
        this.coTgPersistentEntityWithProperties = coTgPersistentEntityWithProperties;
    }

    @Override
    protected TgPersistentEntityWithProperties provideDefaultValues(final TgPersistentEntityWithProperties entity) {
        final IFetchProvider<TgPersistentEntityWithProperties> fetchStrategy = coTgPersistentEntityWithProperties.getFetchProvider();
        final TgPersistentEntityWithProperties defValue =
                //                coTgPersistentEntityWithProperties.getEntity(from(
                //                select(TgPersistentEntityWithProperties.class).where().prop("key").eq().val("DEFAULT_KEY").modelAsEntity(TgPersistentEntityWithProperties.class)
                //                ).with(fetchOnly(TgPersistentEntityWithProperties.class).with("key")).model());
                //                coTgPersistentEntityWithProperties.findById(12L, fetchOnly(TgPersistentEntityWithProperties.class).with("key"));
                coTgPersistentEntityWithProperties.findByKeyAndFetch(fetchStrategy.<TgPersistentEntityWithProperties> fetchFor("producerInitProp").fetchModel(), "DEFAULT_KEY");

        entity.setProducerInitProp(defValue);
        return entity;
    }
}