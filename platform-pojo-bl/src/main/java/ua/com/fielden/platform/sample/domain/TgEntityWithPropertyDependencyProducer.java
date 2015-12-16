package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgEntityWithPropertyDependency}.
 *
 * @author TG Team
 *
 */
public class TgEntityWithPropertyDependencyProducer extends DefaultEntityProducerWithContext<TgEntityWithPropertyDependency, TgEntityWithPropertyDependency> implements IEntityProducer<TgEntityWithPropertyDependency> {
    private final ITgEntityWithPropertyDependency coTgEntityWithPropertyDependency;

    @Inject
    public TgEntityWithPropertyDependencyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgEntityWithPropertyDependency coTgEntityWithPropertyDependency) {
        super(factory, TgEntityWithPropertyDependency.class, companionFinder);
        this.coTgEntityWithPropertyDependency = coTgEntityWithPropertyDependency;
    }

    @Override
    protected TgEntityWithPropertyDependency provideDefaultValues(final TgEntityWithPropertyDependency entity) {
        entity.setKey("DUMMY");
        return entity;
    }
}