package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgStatusActivationFunctionalEntity> {

    @Inject
    public TgStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgStatusActivationFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgStatusActivationFunctionalEntity provideDefaultValues(final TgStatusActivationFunctionalEntity entity) {
        entity.setSelectedEntityId(getContext().getCurrEntity().getId());
        return entity;
    }
}