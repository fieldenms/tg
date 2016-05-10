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
public class TgCollectionalSerialisationParentProducer extends EntityProducerWithNewEditActions<TgCollectionalSerialisationParent> implements IEntityProducer<TgCollectionalSerialisationParent> {

    @Inject
    public TgCollectionalSerialisationParentProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgCollectionalSerialisationParent.class, companionFinder);
    }

    @Override
    protected TgCollectionalSerialisationParent provideDefaultValuesForNewEntity(final TgCollectionalSerialisationParent entity) {
        entity.setKey("DUMMY");
        entity.resetMetaState();
        return entity;
    }
}