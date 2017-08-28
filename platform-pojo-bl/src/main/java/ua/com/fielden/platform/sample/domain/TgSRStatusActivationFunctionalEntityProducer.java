package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgSRStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgSRStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgSRStatusActivationFunctionalEntity> {

    @Inject
    public TgSRStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgSRStatusActivationFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgSRStatusActivationFunctionalEntity provideDefaultValues(final TgSRStatusActivationFunctionalEntity entity) {
        entity.setSelectedEntityId(getContext().getCurrEntity().getId());
        return entity;
    }
}