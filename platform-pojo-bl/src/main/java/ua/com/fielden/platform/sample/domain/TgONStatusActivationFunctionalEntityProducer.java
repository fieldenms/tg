package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgONStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgONStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgONStatusActivationFunctionalEntity> implements IEntityProducer<TgONStatusActivationFunctionalEntity> {

    @Inject
    public TgONStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgONStatusActivationFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgONStatusActivationFunctionalEntity provideDefaultValues(final TgONStatusActivationFunctionalEntity entity) {
        entity.setSelectedEntityId(getContext().getCurrEntity().getId());
        return entity;
    }
}