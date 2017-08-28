package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

public class EntityManipulationActionProducer<T extends AbstractEntityManipulationAction> extends DefaultEntityProducerWithContext<T> {

    @Inject
    public EntityManipulationActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        if (contextNotEmpty()) {
            // final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext();
            final AbstractEntity<?> currEntity = currentEntity();
            final EnhancedCentreEntityQueryCriteria<?, ?> selCrit = selectionCrit();
            final Class<AbstractEntity<?>> entityType = 
                computation().map( computation -> {
                        final Object computed = computation.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext());
                        // it is by convention that a computational context may return custom entity type of tg-entity-master to be displayed
                        // if the type of the result if either Class or T2 representing a tuple of Type (Class) and ID (Long)
                        if (computed instanceof Class) {
                            return (Class<AbstractEntity<?>>) computed;
                        } else if (computed instanceof T2) {
                            final T2<Class<AbstractEntity<?>>, Long> typeAndId = (T2<Class<AbstractEntity<?>>, Long>) computed; 
                            return typeAndId._1;
                        } else {
                            return determineEntityType(currEntity, selCrit);
                        }
                    })
                .orElse(determineEntityType(currEntity, selCrit));
            
            if (entityType == null) {
                throw new IllegalStateException("Please add selection criteria or current entity to the context of the functional entity with type: " + entity.getType().getName());
            } else {
                entity.setEntityType(entityType.getName());
                entity.setImportUri("/master_ui/" + entityType.getName());
                entity.setElementName("tg-" + entityType.getSimpleName() + "-master");
            }
        }
        return entity;
    }

    /**
     * Determines the type of tg-entity-master to be displayed from a) selCrit or b) currEntity depending on whether it is {@link EntityNewAction} or {@link EntityEditAction}.
     * 
     * @param currEntity
     * @param selCrit
     * @return
     */
    private Class<AbstractEntity<?>> determineEntityType(final AbstractEntity<?> currEntity, final EnhancedCentreEntityQueryCriteria<?, ?> selCrit) {
        return selCrit != null ? (Class<AbstractEntity<?>>) selCrit.getEntityClass() :
               currEntity != null ? getOriginalType(currEntity.getType()) : null;
    }
}
