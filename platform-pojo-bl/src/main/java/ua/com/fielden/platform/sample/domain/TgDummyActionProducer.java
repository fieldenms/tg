package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgDummyAction}.
 *
 * @author TG Team
 *
 */
public class TgDummyActionProducer extends DefaultEntityProducerWithContext<TgDummyAction, TgDummyAction> implements IEntityProducer<TgDummyAction> {

    @Inject
    public TgDummyActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgDummyAction.class, companionFinder);
    }

    @Override
    protected TgDummyAction provideDefaultValues(final TgDummyAction entity) {
        entity.setKey("DUMMY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        
        return entity;
    }
}