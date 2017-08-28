package ua.com.fielden.platform.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer of {@link EntityDeleteAction} that initialises entity IDs / type from context's selected entities.
 * 
 * @author TG Team
 *
 */
public class EntityDeleteActionProducer extends DefaultEntityProducerWithContext<EntityDeleteAction> implements IEntityProducer<EntityDeleteAction> {

    @Inject
    public EntityDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityDeleteAction.class, companionFinder);
    }

    @Override
    protected EntityDeleteAction provideDefaultValues(final EntityDeleteAction entity) {
        final List<AbstractEntity<?>> selectedEntities = getContext().getSelectedEntities();
        if (!selectedEntities.isEmpty()) {
            entity.setEntityType(selectedEntities.get(0).getType());
            
            entity.setSelectedEntityIds(
                selectedEntities.stream()
                .map(selectedEntity -> selectedEntity.getId())
                .collect(Collectors.toSet())
            );
        }
        return entity;
    }
}
