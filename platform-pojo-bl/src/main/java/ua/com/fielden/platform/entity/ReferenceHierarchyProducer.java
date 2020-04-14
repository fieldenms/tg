package ua.com.fielden.platform.entity;

import org.apache.commons.lang.StringUtils;

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
            final AbstractEntity<?> selectedEntity = selectedEntities().get(0);
            entity.setRefEntityId(selectedEntity.getId());
            entity.setRefEntityType(selectedEntity.getType().getName());
            entity.setTitle(selectedEntity.getKey() + (StringUtils.isEmpty(selectedEntity.getDesc()) ? "" : ": " + selectedEntity.getDesc()));
        }
        return entity;
    }
}
