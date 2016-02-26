package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.centre.CentreContext;

import com.google.inject.Inject;

public class EntityEditActionProducer extends EntityManipulationActionProducer<EntityEditAction> {

    @Inject
    public EntityEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityEditAction.class, companionFinder);
    }

    @Override
    protected EntityEditAction provideDefaultValues(final EntityEditAction entity) {
        final EntityEditAction editedEntity = super.provideDefaultValues(entity);
        if (getCentreContext() != null) {
            final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = getCentreContext();
            final AbstractEntity<?> currEntity = context.getSelectedEntities().size() == 0 ? null : context.getCurrEntity();
            if (currEntity != null) {
                editedEntity.setEntityId(currEntity == null ? null : currEntity.getId().toString());
            } else {
                throw new IllegalStateException("The edit action context must contain current entity!");
            }
        }
        return editedEntity;
    }
}
