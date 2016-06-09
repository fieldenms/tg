package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.CentreContext;

import com.google.inject.Inject;

public class EntityManipulationActionProducer<T extends AbstractEntityManipulationAction> extends DefaultEntityProducerWithContext<T> {

    @Inject
    public EntityManipulationActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        if (entity.getContext() != null) {
            final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) entity.getContext();
            final AbstractEntity<?> currEntity = context.getSelectedEntities().size() == 0 ? null : context.getCurrEntity();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> selCrit = context.getSelectionCrit();
            final Class<AbstractEntity<?>> entityType = selCrit == null ?
                    (currEntity == null ? null : DynamicEntityClassLoader.getOriginalType(currEntity.getType()))
                    : selCrit.getEntityClass();
            if (entityType == null) {
                throw new IllegalStateException("Please add selection criteria or current entity to the context of the functional entity with type: " + entity.getType().getName());
            } else {
                entity.setContext(context);
                entity.setEntityType(entityType.getName());
                entity.setImportUri("/master_ui/" + entityType.getName());
                entity.setElementName("tg-" + entityType.getSimpleName() + "-master");
            }
        }
        return entity;
    }
}
