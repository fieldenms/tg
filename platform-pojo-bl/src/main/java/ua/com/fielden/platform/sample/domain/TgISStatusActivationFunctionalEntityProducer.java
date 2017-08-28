package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgISStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgISStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgISStatusActivationFunctionalEntity> implements IEntityProducer<TgISStatusActivationFunctionalEntity> {

    @Inject
    public TgISStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgISStatusActivationFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgISStatusActivationFunctionalEntity provideDefaultValues(final TgISStatusActivationFunctionalEntity entity) {
        entity.setSelectedEntityId(getContext().getCurrEntity().getId());
        return entity;
    }
}