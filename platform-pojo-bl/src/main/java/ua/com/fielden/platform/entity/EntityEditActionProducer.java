package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.centre.CentreContext;

public class EntityEditActionProducer extends EntityManipulationActionProducer<EntityEditAction> {

    @Inject
    public EntityEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityEditAction.class, companionFinder);
    }

    @Override
    protected EntityEditAction provideDefaultValues(final EntityEditAction entity) {
        final EntityEditAction editedEntity = super.provideDefaultValues(entity);
        if (getContext() != null) {
            final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext();
            final AbstractEntity<?> currEntity = context.getSelectedEntities().size() == 0 ? null : context.getCurrEntity();
            if (currEntity != null && currEntity.getId() != null) {
                editedEntity.setEntityId(currEntity.getId().toString());
            } else {
                throw new IllegalStateException("The edit action context must contain current entity with its ID property present!");
            }
        }
        return editedEntity;
    }
}
