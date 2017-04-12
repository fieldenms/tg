package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.web.centre.CentreContext;

import com.google.inject.Inject;

public class EntityManipulationActionProducer<T extends AbstractEntityManipulationAction> extends DefaultEntityProducerWithContext<T> {

    @Inject
    public EntityManipulationActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        if (getContext() != null) {
            final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext();
            final AbstractEntity<?> currEntity = context.getSelectedEntities().size() == 0 ? null : context.getCurrEntity();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> selCrit = context.getSelectionCrit();
            final Class<AbstractEntity<?>> entityType;
            if (context.getComputation().isPresent()) {
                final Object computed = context.getComputation().get().apply(entity);
                if (computed instanceof Class) { // it is assumed that computation function returns custom entity type of tg-entity-master to be displayed.
                    entityType = (Class<AbstractEntity<?>>) computed;
                } else {
                    entityType = determineEntityType(currEntity, selCrit);
                }
            } else {
                entityType = determineEntityType(currEntity, selCrit);
            }
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
    private Class<AbstractEntity<?>> determineEntityType(final AbstractEntity<?> currEntity, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> selCrit) {
        return selCrit != null ? selCrit.getEntityClass() :
               currEntity != null ? DynamicEntityClassLoader.getOriginalType(currEntity.getType()) : null;
    }
}
