package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgStatusActivationFunctionalEntity, TgStatusActivationFunctionalEntity> implements IEntityProducer<TgStatusActivationFunctionalEntity> {
    private final ITgStatusActivationFunctionalEntity companion;

    @Inject
    public TgStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgStatusActivationFunctionalEntity companion) {
        super(factory, TgStatusActivationFunctionalEntity.class, companionFinder);
        this.companion = companion;
    }

    @Override
    protected TgStatusActivationFunctionalEntity provideDefaultValues(final TgStatusActivationFunctionalEntity entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}