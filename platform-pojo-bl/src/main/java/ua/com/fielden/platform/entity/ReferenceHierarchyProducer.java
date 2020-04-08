package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class ReferenceHierarchyProducer extends DefaultEntityProducerWithContext<ReferenceHierarchy> {

    @Inject
    public ReferenceHierarchyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, ReferenceHierarchy.class, companionFinder);
    }

    @Override
    protected ReferenceHierarchy provideDefaultValues(final ReferenceHierarchy entity) {
        if (selectedEntitiesNotEmpty()) {
            entity.setRefEntityId(selectedEntities().get(0).getId());
            entity.setRefEntityType(selectedEntities().get(0).getType().getName());
        }
        return entity;
    }
}
