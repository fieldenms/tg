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
public class TgCollectionalSerialisationParentProducer extends EntityProducerWithNewEditActions<TgCollectionalSerialisationParent, TgCollectionalSerialisationParent> implements IEntityProducer<TgCollectionalSerialisationParent> {
    private final ITgCollectionalSerialisationParent co;

    @Inject
    public TgCollectionalSerialisationParentProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgCollectionalSerialisationParent co) {
        super(factory, TgCollectionalSerialisationParent.class, companionFinder);
        this.co = co;
    }

    @Override
    protected TgCollectionalSerialisationParent provideDefaultValuesForNewEntity(final TgCollectionalSerialisationParent entity) {
        entity.setKey("DUMMY");
        return entity;
    }
}