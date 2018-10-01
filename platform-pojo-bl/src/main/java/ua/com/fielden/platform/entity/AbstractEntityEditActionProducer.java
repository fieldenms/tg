package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Abstract producer for entities those extend the {@link EntityEditAction}
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AbstractEntityEditActionProducer<T extends EntityEditAction> extends EntityManipulationActionProducer<T> {

    public AbstractEntityEditActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        final T editedEntity = super.provideDefaultValues(entity);
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
                            throw new IllegalStateException("There is nothing to edit.");
                        }
                    });
            editedEntity.setEntityId(id.toString());

        }
        return editedEntity;
    }
}
