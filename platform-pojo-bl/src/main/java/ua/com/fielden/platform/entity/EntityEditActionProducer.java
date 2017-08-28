package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Producer for {@link EntityEditAction}.
 * 
 * @author TG Team
 *
 */
public class EntityEditActionProducer extends EntityManipulationActionProducer<EntityEditAction> {

    @Inject
    public EntityEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityEditAction.class, companionFinder);
    }

    @Override
    protected EntityEditAction provideDefaultValues(final EntityEditAction entity) {
        final EntityEditAction editedEntity = super.provideDefaultValues(entity);
        if (contextNotEmpty()) {
            final AbstractEntity<?> currEntity = currentEntity();
            // in a polymorphic UI case, IDs may come from a computational context
            // it is by convention that a computational context may return a value of type T2 representing a tuple of Type (Class) and ID (Long)
            final Long id = 
                    computation()
                    .map(computation -> computation.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext()))
                    .filter(computed -> computed instanceof T2)
                    .map(computed -> ((T2<Class<AbstractEntity<?>>, Long>) computed)._2)
                    .orElseGet(() -> {
                        if (currEntity != null && currEntity.getId() != null) {
                            return currEntity.getId();
                        } else {
                            throw new IllegalStateException("The edit action context must contain current entity with its ID property present!");
                        } 
                    });
            editedEntity.setEntityId(id.toString());
            
        }
        return editedEntity;
    }
}
