package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.CentreContext;

import com.google.inject.Inject;

public class EntityManipulationActionProducer<T extends AbstractEntityManipulationAction> extends DefaultEntityProducerWithContext<T, AbstractEntity<?>> {

    @Inject
    public EntityManipulationActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = getCentreContext();
            //final AbstractEntity<?> currEntity = context.getSelectedEntities().size() == 0 ? null : context.getCurrEntity();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> selCrit = context.getSelectionCrit();
            final Class<AbstractEntity<?>> entityType = selCrit.getEntityClass();
            entity.setContext(context);
            //entity.setEntityId(currEntity == null ? null : currEntity.getId());
            entity.setEntityType(entityType.getName());
            entity.setImportUri("/master_ui/" + entityType.getName());
            entity.setElementName("tg-" + entityType.getSimpleName() + "-master");
        }
        return entity;
    }
}
