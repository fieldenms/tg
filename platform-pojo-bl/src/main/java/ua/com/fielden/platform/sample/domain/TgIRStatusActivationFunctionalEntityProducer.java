package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducer;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgIRStatusActivationFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgIRStatusActivationFunctionalEntityProducer extends DefaultEntityProducer<TgIRStatusActivationFunctionalEntity> implements IEntityProducer<TgIRStatusActivationFunctionalEntity> {
    private final ITgStatusActivationFunctionalEntity companion;

    @Inject
    public TgIRStatusActivationFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgStatusActivationFunctionalEntity companion) {
        super(factory, TgIRStatusActivationFunctionalEntity.class, companionFinder);
        this.companion = companion;
    }

    @Override
    protected TgIRStatusActivationFunctionalEntity provideDefaultValues(final TgIRStatusActivationFunctionalEntity entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}