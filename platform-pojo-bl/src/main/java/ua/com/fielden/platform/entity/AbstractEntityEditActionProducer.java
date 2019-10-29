package ua.com.fielden.platform.entity;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Abstract producer for entities those extend the {@link EntityEditAction}
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AbstractEntityEditActionProducer<T extends EntityEditAction> extends EntityManipulationActionProducer<T> {

    private static final Supplier<? extends IllegalStateException> NOTHING_TO_EDIT_EXCEPTION_SUPPLIER = () -> new IllegalStateException("There is nothing to edit.");
    
    public AbstractEntityEditActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        final T editedEntity = super.provideDefaultValues(entity);
        if (contextNotEmpty()) {
            // in a polymorphic UI case, IDs may come from a computational context
            // it is by convention that a computational context may return a value of type T2 representing a tuple of Type (Class) and ID (Long)
            final Long id =
                    computation()
                    .map(computation -> computation.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext()))
                    .filter(computed -> computed instanceof T2)
                    .map(computed -> ((T2<Class<AbstractEntity<?>>, Long>) computed)._2)
                    .orElseGet(() -> {
                        final AbstractEntity<?> currEntity = currentEntity();
                        final Class<? extends AbstractEntity<?>> entityTypeForMaster = editedEntity.getEntityTypeAsClass();
                        // current entity is the entity to be represented by master
                        if (currentEntityInstanceOf(entityTypeForMaster) && (chosenPropertyRepresentsThisColumn() || chosenPropertyEmpty())) {
                            return ofNullable(currEntity.getId()).orElseThrow(NOTHING_TO_EDIT_EXCEPTION_SUPPLIER);
                        } else if (currentEntityNotEmpty() && chosenPropertyNotEmpty()) {
                            // there are two possible legitimate cases here:
                            // 1. either currentEntity().get(chosenProperty()) is of type for Entity Master and all is good, or
                            // 2. chosenProperty is a sub property of a property of type for Entity MAster, where that "parent" property belongs to the current entity, or
                            // 3. we have a genuine bug and need to throw an appropriate error
                            final Optional<? extends AbstractEntity<?>> optClickedEntity = EntityUtils.traversePropPath(currEntity, chosenProperty())
                                    .filter(t2 -> t2._2.map(v -> entityTypeForMaster.isAssignableFrom(v.getType())).orElse(false)) // find only type-compatible values on path
                                    .map(t2 -> entityTypeForMaster.cast(t2._2.get())).findFirst();
                            return optClickedEntity.orElseThrow(NOTHING_TO_EDIT_EXCEPTION_SUPPLIER).getId();
                        } else if (currEntity != null && currEntity.getId() != null) { // "last resort" situation where we assume that ID is for the current entity
                            return currEntity.getId();
                        } else {
                            throw NOTHING_TO_EDIT_EXCEPTION_SUPPLIER.get();
                        }
                    });
            editedEntity.setEntityId(id.toString());
        }
        return editedEntity;
    }
}
