package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgONStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgONStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgONStatusActivationFunctionalEntity, TgONStatusActivationFunctionalEntity> implements IEntityProducer<TgONStatusActivationFunctionalEntity> {
    private final ITgStatusActivationFunctionalEntity companion;

    @Inject
    public TgONStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgStatusActivationFunctionalEntity companion) {
        super(factory, TgONStatusActivationFunctionalEntity.class, companionFinder);
        this.companion = companion;
    }

    @Override
    protected TgONStatusActivationFunctionalEntity provideDefaultValues(final TgONStatusActivationFunctionalEntity entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}