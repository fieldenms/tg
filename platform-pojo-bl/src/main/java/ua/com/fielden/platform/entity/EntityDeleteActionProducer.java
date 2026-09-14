package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

import java.util.Set;

/// Producer of [EntityDeleteAction] that initialises entity IDs / type from context's selected entities.
///
/// The centre's `computation` may refine the defaults:
/// - `Class<? extends AbstractEntity<?>>` overrides only the entity type; selected IDs still come from the context.
/// - `T2<Class<? extends AbstractEntity<?>>, Long>` targets a single entity by `(type, id)`, ignoring the context's selected IDs — used by property-action deletion driven by a chosen-property.
///
/// Any other computation result falls back to the defaults.
///
public class EntityDeleteActionProducer extends DefaultEntityProducerWithContext<EntityDeleteAction> {

    @Inject
    public EntityDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityDeleteAction.class, companionFinder);
    }

    @Override
    protected EntityDeleteAction provideDefaultValues(final EntityDeleteAction entity) {
        if (selectedEntitiesNotEmpty()) {
            final Class<? extends AbstractEntity<?>> fallbackType = selectedEntities().getFirst().getType();
            final Object computed = computation()
                    .map(comp -> comp.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext()))
                    .orElse(fallbackType);

            if (computed instanceof Class<?> type && AbstractEntity.class.isAssignableFrom(type)) {
                entity.setEntityType((Class<? extends AbstractEntity<?>>) type);
                entity.setSelectedEntityIds(selectedEntityIds());
            }
            // Support for targeted chosenProperty-based deletion, where deletion is a property action.
            else if (computed instanceof T2<?, ?> t2
                     && t2._1() instanceof Class<?> type
                     && AbstractEntity.class.isAssignableFrom(type)
                     && t2._2() instanceof Long id)
            {
                entity.setEntityType((Class<? extends AbstractEntity<?>>) type);
                entity.setSelectedEntityIds(Set.of(id));
            }
            // A fallback in case none of the above conditions matched.
            else {
                entity.setEntityType(fallbackType);
                entity.setSelectedEntityIds(selectedEntityIds());
            }
        }
        return entity;
    }
}
