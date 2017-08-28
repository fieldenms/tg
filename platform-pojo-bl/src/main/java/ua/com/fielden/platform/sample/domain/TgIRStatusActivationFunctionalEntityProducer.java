package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgIRStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgIRStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgIRStatusActivationFunctionalEntity> {

    @Inject
    public TgIRStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgIRStatusActivationFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgIRStatusActivationFunctionalEntity provideDefaultValues(final TgIRStatusActivationFunctionalEntity entity) {
        entity.setSelectedEntityId(getContext().getCurrEntity().getId());
        return entity;
    }
}