package ua.com.fielden.platform.sample.domain.compound.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;

/**
 * A producer for new instances of entity {@link TgCompoundEntity}.
 *
 * @author TG Team
 *
 */
public class TgCompoundEntityProducer extends DefaultEntityProducerWithContext<TgCompoundEntity> {

    @Inject
    public TgCompoundEntityProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgCompoundEntity.class, coFinder);
    }

    @Override
    protected TgCompoundEntity provideDefaultValues(final TgCompoundEntity entity) {
        if (keyOfMasterEntityInstanceOf(TgCompoundEntity.class)) {
            final TgCompoundEntity instance = keyOfMasterEntity(TgCompoundEntity.class);
            if (instance.isPersisted()) {
                return refetchInstrumentedEntityById(instance.getId());
            }
        }
        return entity;
    }

}