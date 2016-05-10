package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.EntityProducerWithNewEditActions;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgEntityWithPropertyDependency}.
 *
 * @author TG Team
 *
 */
public class TgEntityWithPropertyDependencyProducer extends EntityProducerWithNewEditActions<TgEntityWithPropertyDependency> implements IEntityProducer<TgEntityWithPropertyDependency> {

    @Inject
    public TgEntityWithPropertyDependencyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgEntityWithPropertyDependency.class, companionFinder);
    }

    @Override
    protected TgEntityWithPropertyDependency provideDefaultValuesForNewEntity(final TgEntityWithPropertyDependency entity) {
        entity.setKey("DUMMY");
        entity.resetMetaState();
        return entity;
    }
}