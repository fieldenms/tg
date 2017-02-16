package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;

/**
 * A producer for new instances of entity {@link TgPersistentEntityWithProperties}.
 *
 * @author TG Team
 *
 */
public class TgPersistentEntityWithPropertiesProducer extends DefaultEntityProducerWithContext<TgPersistentEntityWithProperties> {
    private final ITgPersistentEntityWithProperties coTgPersistentEntityWithProperties;
    private final ISerialiser serialiser;

    @Inject
    public TgPersistentEntityWithPropertiesProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgPersistentEntityWithProperties coTgPersistentEntityWithProperties, final ISerialiser serialiser) {
        super(factory, TgPersistentEntityWithProperties.class, companionFinder);
        this.coTgPersistentEntityWithProperties = coTgPersistentEntityWithProperties;
        this.serialiser = serialiser;
    }

    @Override
    public TgPersistentEntityWithProperties provideDefaultValuesForStandardNew(final TgPersistentEntityWithProperties entity, final EntityNewAction masterEntity) {
        return provideProducerInitProp(coTgPersistentEntityWithProperties, entity, serialiser);
    }
    
    @Override
    public TgPersistentEntityWithProperties provideDefaultValues(final TgPersistentEntityWithProperties entity) {
        return provideProducerInitProp(coTgPersistentEntityWithProperties, entity, serialiser);
    }
    
    private static TgPersistentEntityWithProperties provideProducerInitProp(final ITgPersistentEntityWithProperties co, final TgPersistentEntityWithProperties entity, final ISerialiser serialiser) {
        final IFetchProvider<TgPersistentEntityWithProperties> fetchStrategy = co.getFetchProvider();
        final TgPersistentEntityWithProperties defValue =
                //                coTgPersistentEntityWithProperties.getEntity(from(
                //                select(TgPersistentEntityWithProperties.class).where().prop("key").eq().val("DEFAULT_KEY").modelAsEntity(TgPersistentEntityWithProperties.class)
                //                ).with(fetchOnly(TgPersistentEntityWithProperties.class).with("key")).model());
                //                coTgPersistentEntityWithProperties.findById(12L, fetchOnly(TgPersistentEntityWithProperties.class).with("key"));
                co.findByKeyAndFetch(fetchStrategy.<TgPersistentEntityWithProperties> fetchFor("producerInitProp").fetchModel(), "DEFAULT_KEY");

        entity.setProducerInitProp(defValue);
        
        final TgJackson tgJackson = (TgJackson) serialiser.getEngine(SerialiserEngines.JACKSON);
        final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = tgJackson.idOnlyProxiedEntityTypeCache;
        final TgPersistentEntityWithProperties idOnlyProxy = EntityFactory.newPlainEntity(idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor(TgPersistentEntityWithProperties.class), 7L);
        
        entity.setIdOnlyProxyProp(idOnlyProxy);
        return entity;
    }
}