package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgISStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgISStatusActivationFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgISStatusActivationFunctionalEntity, TgISStatusActivationFunctionalEntity> implements IEntityProducer<TgISStatusActivationFunctionalEntity> {
    private final ITgStatusActivationFunctionalEntity companion;

    @Inject
    public TgISStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgStatusActivationFunctionalEntity companion) {
        super(factory, TgISStatusActivationFunctionalEntity.class, companionFinder);
        this.companion = companion;
    }

    @Override
    protected TgISStatusActivationFunctionalEntity provideDefaultValues(final TgISStatusActivationFunctionalEntity entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}