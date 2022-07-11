package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.centre.CentreContext;

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
            final Class<? extends AbstractEntity<?>> fallbackType = selectedEntities().get(0).getType();
            final Class<? extends AbstractEntity<?>> entityType =
                        computation().map(comp -> {
                            final Object computed = comp.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext());
                            if (computed instanceof Class && AbstractEntity.class.isAssignableFrom((Class) computed)) {
                                return (Class<? extends AbstractEntity<?>>) computed;
                            } else {
                                return fallbackType;
                            }
                        }).orElse(fallbackType);
            entity.setEntityType(entityType);
            entity.setSelectedEntityIds(selectedEntityIds());
        }
        return entity;
    }
}
