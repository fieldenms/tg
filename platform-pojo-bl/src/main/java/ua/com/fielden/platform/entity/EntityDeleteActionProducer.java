package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer of {@link EntityDeleteAction} that initialises entity IDs / type from context's selected entities.
 * 
 * @author TG Team
 *
 */
public class EntityDeleteActionProducer extends DefaultEntityProducerWithContext<EntityDeleteAction> {

    @Inject
    public EntityDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityDeleteAction.class, companionFinder);
    }

    @Override
    protected EntityDeleteAction provideDefaultValues(final EntityDeleteAction entity) {
        if (selectedEntitiesNotEmpty()) {
            entity.setEntityType(selectedEntities().get(0).getType());
            entity.setSelectedEntityIds(selectedEntityIds());
        }
        return entity;
    }
}
