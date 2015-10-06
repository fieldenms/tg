package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgCentreInvokerWithCentreContext}.
 *
 * @author TG Team
 *
 */
public class TgCentreInvokerWithCentreContextProducer extends DefaultEntityProducerWithContext<TgCentreInvokerWithCentreContext, TgCentreInvokerWithCentreContext> implements IEntityProducer<TgCentreInvokerWithCentreContext> {
    @Inject
    public TgCentreInvokerWithCentreContextProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgCentreInvokerWithCentreContext.class, companionFinder);
    }

    @Override
    protected TgCentreInvokerWithCentreContext provideDefaultValues(final TgCentreInvokerWithCentreContext entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}