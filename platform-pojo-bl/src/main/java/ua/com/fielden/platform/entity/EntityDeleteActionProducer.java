package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

public class EntityDeleteActionProducer extends DefaultEntityProducerWithContext<EntityDeleteAction, EntityDeleteAction> {

    @Inject
    public EntityDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityDeleteAction.class, companionFinder);
    }

    @Override
    protected EntityDeleteAction provideDefaultValues(final EntityDeleteAction entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }

}
