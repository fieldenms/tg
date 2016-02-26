package ua.com.fielden.platform.web.test.server.master_action;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link NewEntityAction}.
 *
 * @author TG Team
 *
 */
public class NewEntityActionProducer extends DefaultEntityProducerWithContext<NewEntityAction, NewEntityAction> implements IEntityProducer<NewEntityAction> {

    @Inject
    public NewEntityActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, NewEntityAction.class, companionFinder);
    }

    @Override
    protected NewEntityAction provideDefaultValues(final NewEntityAction entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}