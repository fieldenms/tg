package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgEntityWithPropertyDependency}.
 *
 * @author TG Team
 *
 */
public class TgEntityWithPropertyDependencyProducer extends DefaultEntityProducerWithContext<TgEntityWithPropertyDependency> implements IEntityProducer<TgEntityWithPropertyDependency> {

    @Inject
    public TgEntityWithPropertyDependencyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgEntityWithPropertyDependency.class, companionFinder);
    }

    @Override
    protected TgEntityWithPropertyDependency provideDefaultValuesForStandardNew(final TgEntityWithPropertyDependency entity, final EntityNewAction masterEntity) {
        entity.setKey("DUMMY");
        entity.resetMetaState();
        return entity;
    }
}