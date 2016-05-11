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
public class TgCollectionalSerialisationParentProducer extends DefaultEntityProducerWithContext<TgCollectionalSerialisationParent> implements IEntityProducer<TgCollectionalSerialisationParent> {

    @Inject
    public TgCollectionalSerialisationParentProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgCollectionalSerialisationParent.class, companionFinder);
    }

    @Override
    protected TgCollectionalSerialisationParent provideDefaultValuesForStandardNew(final TgCollectionalSerialisationParent entity, final EntityNewAction masterEntity) {
        entity.setKey("DUMMY");
        entity.resetMetaState();
        return entity;
    }
}