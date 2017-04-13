package ua.com.fielden.platform.entity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class EntityDeleteActionProducer extends DefaultEntityProducerWithContext<EntityDeleteAction> implements IEntityProducer<EntityDeleteAction> {

    @Inject
    public EntityDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityDeleteAction.class, companionFinder);
    }

    @Override
    protected EntityDeleteAction provideDefaultValues(final EntityDeleteAction entity) {
        final List<AbstractEntity<?>> selectedEntities = getContext().getSelectedEntities();
        if (selectedEntities.size() > 0) {
            final Class<? extends AbstractEntity<?>> entityType = selectedEntities.get(0).getType();
            entity.setEntityType(entityType);
            
            final Set<Long> selectedEntityIds = new LinkedHashSet<Long>();
            selectedEntities.forEach(selectedEntity -> selectedEntityIds.add(selectedEntity.getId()));
            entity.setSelectedEntityIds(selectedEntityIds);
        }
        return entity;
    }
}
